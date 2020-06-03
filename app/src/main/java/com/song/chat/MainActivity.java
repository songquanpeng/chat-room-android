package com.song.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final int CHOOSE_FROM_ALBUM = 100;
    public static final String TAG = "MAIN_ACTIVITY";
    private EditText messageInput;
    private Connection connection;
    private String username;
    private String serverAddress;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView messageRecyclerView;
    private MessageAdapter messageAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize web socket connection
        connection = Connection.getInstance();
        sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
        serverAddress = sharedPreferences.getString("serverAddress", "https://chat.iamazing.cn/");
        username = sharedPreferences.getString("username", "Default Username");
        String roomID = sharedPreferences.getString("roomID", "/");
        connection.init(serverAddress, username, roomID,
                onConnect, onDisconnect,
                onConnectError, onMessage,
                onConflictUsername, onRegisterSuccess);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        messageInput = findViewById(R.id.message_input);
        Button sendBtn = findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "user click send btn");
                sendTextMessage();
            }
        });
        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i(TAG, "user long click send btn");
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_FROM_ALBUM);
                return true;
            }
        });

        // Initialize recycle view
        messageRecyclerView = findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messageRecyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList, this, serverAddress);
        messageRecyclerView.setAdapter(messageAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FROM_ALBUM) {
            if (resultCode == RESULT_OK) {
                try {
                    if (data != null) {
                        File file = getImageFromUri(data.getData());
                        if (file != null) {
                            uploadImage(file);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File getImageFromUri(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        if (input != null) {
            input.close();
        }
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        float hh = 720f;
        float ww = 480f;
        int zoomRate = 1;
        if (originalWidth > originalHeight && originalWidth > ww) {
            zoomRate = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {
            zoomRate = (int) (originalHeight / hh);
        }
        if (zoomRate <= 0)
            zoomRate = 1;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = zoomRate;
        input = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        if (input != null) {
            input.close();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        }
        long currentTimeMillis = System.currentTimeMillis();
        File outputImage = new File(getExternalCacheDir(), username + currentTimeMillis + ".jpg");
        if (outputImage.exists()) {
            boolean success = outputImage.delete();
            if (!success) {
                Log.i(TAG, "unable to delete temp image.");
            }
        }
        if (outputImage.createNewFile()) {
            FileOutputStream fileOutputStream = new FileOutputStream(outputImage);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        return outputImage;
    }

    private void uploadImage(File file) {
        MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE, file))
                .build();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(serverAddress + "upload/")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = Objects.requireNonNull(response.body()).string();
                String content = null;
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    content = jsonObject.getString("path");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connection.sendMessage(content, "IMAGE");
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i(TAG, e.toString());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connection.destroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.setting_btn) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_setting) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendTextMessage() {
        String messageContent = messageInput.getText().toString().trim();
        if (messageContent.equals("")) return;
        Log.i(TAG, "send message: " + messageContent);
        connection.sendMessage(messageContent, "TEXT");
        messageInput.setText("");

    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.disconnect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.connect_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String content;
                    String sender;
                    String type;
                    try {
                        content = data.getString("content");
                        sender = data.getString("sender");
                        type = data.getString("type");
                        Log.i(TAG, "receive message: " + data.toString());
                        Message message = new Message(content, sender, type, username.equals(sender));
                        messageList.add(message);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        messageRecyclerView.scrollToPosition(messageList.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener onConflictUsername = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.conflict_username, Toast.LENGTH_LONG).show();
                    requireNewUsername();
                }
            });
        }
    };

    private Emitter.Listener onRegisterSuccess = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };

    void requireNewUsername() {
        final EditText edit = new EditText(this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
        inputDialog.setTitle(getString(R.string.dialog_require_username));
        inputDialog.setMessage("Please input: ");
        inputDialog.setIcon(R.drawable.ic_launcher);
        inputDialog.setView(edit);
        inputDialog.setPositiveButton(getString(R.string.dialog_btn_confirm_text)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        username = edit.getText().toString().trim();
                        connection.registerUsername(username);
                        saveUsername(username);
                        dialog.dismiss();
                    }
                });

        inputDialog.create().show();
    }

    void saveUsername(String username) {
        this.username = username;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();
    }
}

