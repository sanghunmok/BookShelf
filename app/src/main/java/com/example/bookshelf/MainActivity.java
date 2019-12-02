package com.example.bookshelf;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.bookshelf.PDFAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lv_pdf;
    public static ArrayList<File> fileList = new ArrayList<>();
    PDFAdapter obj_adapter;
    public static int REQUEST_PERMISSION = 1;
    boolean bolean_permission;
    File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv_pdf = findViewById(R.id.listView_pdf);

        dir = new File(Environment.getExternalStorageDirectory().toString());
        System.out.println(Environment.getExternalStorageDirectory().toString());
        permission_fn();

        lv_pdf.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int itemPosition, long l) {

                Log.d("MyTag", fileList.get(itemPosition).getAbsolutePath());

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // Create List Adapter
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        android.R.layout.select_dialog_item);
                adapter.add("Share File");
                adapter.add("Change File Name");
                adapter.add("Delete File");

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {
                            File file = new File(fileList.get(itemPosition).getAbsolutePath());

                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.example.bookshelf.fileprovider", file);

                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.setType("application/*");

                            Intent chooser = Intent.createChooser(intent, "Share File");
                            startActivity(chooser);
                        } else if (i == 1) {
                            final EditText et = new EditText(MainActivity.this);
                            final AlertDialog.Builder fnBuilder = new AlertDialog.Builder(MainActivity.this);

                            et.setText(fileList.get(itemPosition).getName());

                            fnBuilder.setTitle("Chagne File Name.")
                                    .setMessage("Enter new file name.")
                                    .setCancelable(false)
                                    .setView(et)
                                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {
                                            String value = et.getText().toString();

                                            File file = new File(fileList.get(itemPosition).getAbsolutePath());
                                            File newFile = new File(file.getParent() + "/" + value + ".pdf");

                                            if(file.renameTo(newFile)) {
                                                fileList.clear();
                                                getfile(dir);
                                                obj_adapter.notifyDataSetChanged();
                                                Toast.makeText(MainActivity.this, "File has been renamed.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Renaming has failed, try again", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            AlertDialog alert = fnBuilder.create();
                            alert.show();
                        } else {
                            AlertDialog.Builder fdBuilder = new AlertDialog.Builder(MainActivity.this);
                            fdBuilder.setMessage("Would you like to delete the file?");
                            fdBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            fdBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File file = new File(fileList.get(itemPosition).getAbsolutePath());

                                    try {
                                        file.delete();
                                        fileList.clear();
                                        getfile(dir);
                                        obj_adapter.notifyDataSetChanged();
                                        Toast.makeText(MainActivity.this, "File has been deleted", Toast.LENGTH_SHORT).show();
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(MainActivity.this, "Deletion failed, try again", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            AlertDialog dialog = fdBuilder.create();
                            dialog.show();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        lv_pdf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ViewPDFFiles.class);
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });
    }

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int MULTIPLE_PERMISSIONS = 101;

    private boolean permission_fn() {
        int result;

        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else {
            bolean_permission = true;
            getfile(dir);
            obj_adapter = new PDFAdapter(getApplicationContext(), fileList);
            lv_pdf.setAdapter(obj_adapter);
        }
        return true;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    boolean permissionAllow = true;
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                permissionAllow = false;
                                Toast.makeText(this, "Please Allow the Permission", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    if(permissionAllow) {
                        bolean_permission = true;
                        getfile(dir);
                        obj_adapter = new PDFAdapter(getApplicationContext(), fileList);
                        lv_pdf.setAdapter(obj_adapter);
                    }
                } else {
                    Toast.makeText(this, "Please Allow the Permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public ArrayList<File> getfile(File dir) {
        File listFile[] = dir.listFiles();

        if (listFile != null && listFile.length > 0) {

            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    getfile(listFile[i]);
                } else {
                    boolean booleanpdf = false;
                    if (listFile[i].getName().endsWith(".pdf")) {

                        for (int j = 0; j < fileList.size(); j++) {

                            if (fileList.get(j).getName().equals(listFile[i].getName())) {
                                booleanpdf = true;
                            } else {

                            }
                        }

                        if (booleanpdf) {
                            booleanpdf = false;
                        } else {
                            fileList.add(listFile[i]);
                        }
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                obj_adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

}
