package org.ucomplex.ucomplex.Activities;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ucomplex.ucomplex.Activities.Tasks.FetchMessagesTask;
import org.ucomplex.ucomplex.Activities.Tasks.UploadPhotoTask;
import org.ucomplex.ucomplex.Adaptors.MessagesAdapter;
import org.ucomplex.ucomplex.Common;
import org.ucomplex.ucomplex.Interfaces.OnTaskCompleteListener;
import org.ucomplex.ucomplex.Model.Message;
import org.ucomplex.ucomplex.Model.Users.User;
import org.ucomplex.ucomplex.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity implements OnTaskCompleteListener {

    LinkedList<Message> messageArrayList = new LinkedList<>();
    ListView listView;
    String companion;
    MessagesAdapter messagesAdapter;
    String name;
    String filePath;
    boolean file = false;
    FetchMessagesTask fetchNewMessagesTask;
    TextView nameTextView;
    CircleImageView profileImageView;
    TextView messageTextView;
    Button sendFileButton;
    Button sendMsgButton;
    CircleImageView messageImage;
    CircleImageView messageImageTemp;
    ProgressBar imageProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle("Сообщения");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface robotoFont = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Regular.ttf");
        profileImageView = (CircleImageView) findViewById(R.id.list_messages_toolbar_profile);
        nameTextView = (TextView) findViewById(R.id.list_messages_toolbar_name);
        messageImage = (CircleImageView) findViewById(R.id.message_image);
        messageImageTemp = (CircleImageView) findViewById(R.id.message_image_temp);
        imageProgress = (ProgressBar) findViewById(R.id.imagep_rogress);
        imageProgress.setVisibility(View.INVISIBLE);
        nameTextView.setTypeface(robotoFont);
        companion = getIntent().getStringExtra("companion");
        name = getIntent().getStringExtra("name");

        User aUser = new User();
        aUser.setId(Integer.valueOf(companion));
        aUser.setName(name);
        profileImageView.setImageDrawable(Common.getDrawable(aUser));

        String[] aName = name.split(" ");
        if(aName.length==2){
            nameTextView.setText(aName[0] + " " + aName[1]);
        }else{
            nameTextView.setText(aName[0]);
        }
        messagesAdapter = new MessagesAdapter(this, messageArrayList, companion, name);
        listView = (ListView) findViewById(R.id.list_messages_listview);
        listView.setScrollingCacheEnabled(false);
        fetchNewMessagesTask = new FetchMessagesTask(this, this);
        fetchNewMessagesTask.setType(0);
        fetchNewMessagesTask.setupTask(companion);

        sendMsgButton = (Button) findViewById(R.id.messages_send_button);
        sendMsgButton.setEnabled(false);
        messageTextView = (TextView) findViewById(R.id.messages_text);
        messageTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendMsgButton.setEnabled(true);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        sendMsgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fetchNewMessagesTask = new FetchMessagesTask(MessagesActivity.this, MessagesActivity.this);
                fetchNewMessagesTask.setType(1);
                String message = messageTextView.getText().toString();
                if (file && filePath != null) {
                    String[] splitFilename = filePath.split("/");
                    String filename = splitFilename[splitFilename.length - 1];
                    messageImageTemp.setImageBitmap(null);
                    fetchNewMessagesTask.setupTask(filePath, companion, filename, message);
                    imageProgress.setVisibility(View.VISIBLE);
                    file = false;
                } else {
                    fetchNewMessagesTask.setupTask(companion, messageTextView.getText().toString());
                    imageProgress.setVisibility(View.VISIBLE);
                }
                ObjectAnimator.ofFloat(sendFileButton, "rotation", 1, 0).start();
                messageTextView.setText("");
                scrollMyListViewToBottom();
            }
        });
        sendFileButton = (Button) findViewById(R.id.messages_file_button);
        sendFileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!file){
                    showFileChooser();
                }else{
                    file = false;
                    filePath = "";
                    messageTextView.setText("");
                    messageImageTemp.setImageBitmap(null);
                }
                ObjectAnimator.ofFloat(sendFileButton, "rotation", 1, 0).start();
            }
        });
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (fetchNewMessagesTask == null) {
                    fetchNewMessagesTask = new FetchMessagesTask(MessagesActivity.this, MessagesActivity.this);
                    fetchNewMessagesTask.setType(2);
                    fetchNewMessagesTask.setupTask(companion);
                }
            }
        }, 0, 1000);
    }

    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(messagesAdapter.getCount() - 1);
            }
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Выберите файл для загрузки"),
                    Common.FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Файловый менеджер не установлен",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    super.onActivityResult(requestCode, resultCode, data);
                    if (null == data) return;
                    Uri originalUri = null;
                    if (requestCode == Common.GALLERY_INTENT_CALLED) {
                        originalUri = data.getData();
                    } else if (requestCode == Common.GALLERY_KITKAT_INTENT_CALLED) {
                        originalUri = data.getData();
                        this.grantUriPermission("org.ucomplex.ucomplex.Activities", originalUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    filePath = getPath(originalUri);
                    String[] splitFilename = filePath.split("/");
                    String filename = splitFilename[splitFilename.length - 1];
                    String[] fileNameArray = filename.split("\\.");
                    String format = fileNameArray[fileNameArray.length-1];
//                    messageTextView.setText("Файл: "+filename);
                    file = true;
                    sendMsgButton.setEnabled(true);

                    ObjectAnimator.ofFloat(sendFileButton, "rotation", 1, 45).start();

                    if(format.equals("jpeg") || format.equals("jpg") || format.equals("png")){
                        File image = new File(filePath);
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                        if(bitmap!=null){
                            messageImageTemp.setImageBitmap(bitmap);
                        }
                    }else{
                        messageImageTemp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_attachment_dark));
                    }
                    this.revokeUriPermission(originalUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected int sizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else {
            return data.getByteCount();
        }
    }

    public  String getMimeType(String filePath) {
        String[] parts = filePath.split(".");
        return parts[parts.length-1];
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onTaskComplete(AsyncTask task, Object... o) {
        if (task.isCancelled()) {

            Toast.makeText(this, "Операция была прервана", Toast.LENGTH_LONG)
                    .show();
        } else {
            try {
                if (task instanceof UploadPhotoTask) {

                } else {
                    FetchMessagesTask fmt = (FetchMessagesTask) task;
                    if (fmt.getType() == 0) {
                        messageArrayList = (LinkedList) task.get();
                        if (messageArrayList != null) {
                            Collections.reverse(messageArrayList);
                            messagesAdapter = new MessagesAdapter(this, messageArrayList, companion, name);
                            listView.setAdapter(messagesAdapter);
                            listView.setSelection(messagesAdapter.getCount() - 1);
                            if (messagesAdapter.getBitmap() != null) {
                                profileImageView.setImageBitmap(messagesAdapter.getBitmap());
                            }
                        }
                    } else if (fmt.getType() == 1 || fmt.getType() == 2) {

                        LinkedList result = (LinkedList) task.get();
                        if (result != null) {
                            int cycles = 0;
                            if (result.size() > 0) {
                                if (result.get(result.size() - 1) instanceof Bitmap) {
                                    cycles = result.size() - 1;
                                } else {
                                    cycles = result.size();
                                }
                            }

                            if (cycles > 0) {
                                messageArrayList.addLast((Message) result.get(0));
                                if (result.size() > 0) {
                                    messagesAdapter.notifyDataSetChanged();
                                    listView.setSelection(messagesAdapter.getCount() - 1);
                                } else {
                                    Toast.makeText(this, "Ошибка при отправке сообщения", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        }
                        imageProgress.setVisibility(View.INVISIBLE);
                    }
                    fetchNewMessagesTask = null;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
