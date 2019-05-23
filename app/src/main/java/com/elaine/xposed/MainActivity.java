package com.elaine.xposed;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

/**
 * <p>
 * Package Name:com.elaine.xposed
 * </p>
 * <p>
 * Class Name:MainActivity
 * <p>
 * Description:
 * </p>
 *
 * @Author Martin
 * @Version 1.0 2019/1/29 3:23 PM Release
 * @Reviser: 
 * @Modification Time:2019/1/29 3:23 PM
 */
public class MainActivity extends AppCompatActivity {

    public static List<String>  methodJson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
