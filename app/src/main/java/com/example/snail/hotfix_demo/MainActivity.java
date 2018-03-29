package com.example.snail.hotfix_demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView textTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textTv = findViewById(R.id.text_tv);

        try {
            readTestExcel();
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
            e.printStackTrace();
        } catch (BiffException e) {
            Log.e(TAG, "BiffException: " + e);
            e.printStackTrace();
        }
    }


    /**
     * @throws IOException
     * @throws BiffException 读取OnsQuestionExcel文件
     */
    public void readTestExcel() throws IOException, BiffException {

        StringBuffer contentSb = new StringBuffer();

        InputStream inputStream = getAssets().open("test.xls");//输入流
        Workbook book = Workbook.getWorkbook(inputStream);//用读取到的表格文件来实例化工作簿对象（符合常理，我们所希望操作的就是Excel工作簿文件）
        Sheet sheet = book.getSheet(0); // 第一张表
        int Rows = sheet.getRows();//得到当前工作表的行数
        int Cols = sheet.getColumns(); //得到当前工作表的列数

        for (int i = 0; i < Rows; i++) {  // 按行读取
            contentSb.append("\n");
            for (int j = 0; j < Cols; j++) { //按列读取
                String str = sheet.getCell(j, i).getContents();
                contentSb.append(str + "\t\t");
                Log.d(TAG, "第" + i + "行，第" + j + "列的内容：" + str);
            }
        }
        textTv.setText(contentSb);
    }
}
