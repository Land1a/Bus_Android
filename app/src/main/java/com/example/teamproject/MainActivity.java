package com.example.teamproject;

import android.graphics.Color;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.net.URLEncoder; //한글 UrlEncoding을 위함

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {
    //전역변수 설정
    TextView textView;              //id:data인 택스트뷰 오류 및 관련 내용을 출력
    String getData;                 //api를 활용하기 위한 문자열
    EditText stName;                //검색창을 위한 에딧택스트
    LinearLayout screen;            //검색 결과를 출력하기 위한 리니어 레이아웃
    ListView list;                  //검색 기록을 출력하기 위한 리스트 뷰
    ArrayList<String> saveList;     //리스트 뷰에 넣을 값을 활용하기 위한 문자열
    ArrayAdapter<String> adapter;   //리스트 뷰에 값을 넣는 등 활용을 위한 어뎁터
    String urlEncode;               //한글을 UrlEncoding하기 위한 문자열
    int saveCount1, saveCount2;     //반복 횟수를 위해 사용되는 변수
    int i;                          //반복을 위해 사용되는 변수

    String infoDirection;   //방향
    String infoArrTime1;    //다음차
    String infoArrTime2;    //다다음차
    String infoBusNumber;   //버스번호
    String infoInterval;    //배차간격


    Queue<String> stationId = new LinkedList<>();           //버스 ID를 얻어 사용하기 위한 리스트

    LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(   //자바로 만들어지는 레이아웃 파라미터
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );

    LinearLayout.LayoutParams pm = new LinearLayout.LayoutParams(   //자바로 만드는 버튼과 텍스트뷰 파라미터
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.icon_bus);

        textView = (TextView) findViewById(R.id.data);              //id:data인 택스트뷰
        stName = (EditText) findViewById(R.id.stationName);         //검색창을 위한 에딧택스트
        screen = (LinearLayout) findViewById(R.id.linearLayout);    //검색 결과를 출력하기 위한 리니어 레이아웃

        saveList = new ArrayList<String>();                         //리스트 뷰에 넣을 값을 활용하기 위한 문자열
        list = (ListView) findViewById(R.id.listView);              //검색 기록을 출력하기 위한 리스트 뷰

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, saveList);    //리스트 뷰에 값을 넣는 등 활용을 위한 어뎁터
        list.setAdapter(adapter);   //리스트 뷰에 어뎁터 연결

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() { //리스트뷰의 아이템을 클릭할 때
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String listItem = (String) adapterView.getAdapter().getItem(i);
                stName.setText(listItem);   //해당 아이템의 내용을 에딧택스트에 넣기
            }
        });
    }

    public class DownloadWebContent2 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadByUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패1";
            }
        }

        protected void onPostExecute(String result) { //DownloadWebContent1에서 execute()하면 자동 실행, 수동 X;
            String headerCd = "";
            String arsId = "";          //정류소ID(arsId)

            boolean station_headerCd = false;
            boolean station_arsId = false;

            stationId.clear();

            textView.append("-정류소 검색 결과-\n");
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xmlpp = factory.newPullParser();

                xmlpp.setInput(new StringReader(result));
                int eventType = xmlpp.getEventType();

                saveCount1 = 0;
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        ;
                    } else if (eventType == XmlPullParser.START_TAG) {
                        String tag_name = xmlpp.getName();

                        switch (tag_name) {

                            case "headerCd":
                                station_headerCd = true;
                                break;
                            case "arsId":
                                station_arsId = true;
                                break;
                        }

                    } else if (eventType == XmlPullParser.TEXT) {
                        if (station_headerCd) {
                            headerCd = xmlpp.getText();
                            station_headerCd = false;
                        }

                        if (headerCd.equals("0")) {
                            if (station_arsId) {
                                saveCount1++;

                                arsId = xmlpp.getText();
                                stationId.add(arsId);
                                station_arsId = false;
                            }

                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlpp.next();
                }
            } catch (Exception e) {
                textView.setText(e.getMessage());
            }
            for (i =0; i < saveCount1; i++){
                printStation(stationId.poll());
            }
        }



        public String downloadByUrl(String myurl) throws IOException {

            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();

                BufferedInputStream buffer = new BufferedInputStream(conn.getInputStream());

                BufferedReader buffer_reader = new BufferedReader(new InputStreamReader(buffer, "utf-8"));

                String line = null;
                getData = "";
                while ((line = buffer_reader.readLine()) != null) {
                    getData += line;

                }
                return getData;
            } finally {
                conn.disconnect();
            }
        }

    }

    public void printStation(String Id){
        String serviceUrl3 = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid";
        String serviceKey3 = "WNYOOnmIyX4ZIcLeUFZ2M%2BwJ7gZAYGX9%2B2TvPXcD3JJDE%2B57d2BzPnK83dEBcwp1%2Fc5FKqkkEPwSaOYq9Xx4og%3D%3D";
        String strUrl3 = serviceUrl3 + "?ServiceKey=" + serviceKey3 + "&arsId=" + Id;
        DownloadWebContent3 dwc3 = new DownloadWebContent3();
        dwc3.execute(strUrl3);

    }

    public class DownloadWebContent3 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return (String) downloadByUrl((String) urls[0]);
            } catch (IOException e) {
                return "다운로드 실패1";
            }
        }

        protected void onPostExecute(String result) { //DownloadWebContent에서 execute()하면 자동 실행, 수동 X;
            String headerCd = "";
            String adirection = "";     //방향
            String arrmsg1 = "";        //다음차
            String arrmsg2 = "";        //다다음차
            String rtNm = "";           //버스번호
            String term = "";           //배차간격

            boolean bus_headerCd = false;
            boolean bus_adirection = false;
            boolean bus_arrmsg1 = false;
            boolean bus_arrmsg2 = false;
            boolean bus_rtNm = false;
            boolean bus_term = false;
            makeLine();
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xmlpp = factory.newPullParser();

                xmlpp.setInput(new StringReader(result));
                int eventType = xmlpp.getEventType();

                saveCount2 = 0;
                while (eventType != XmlPullParser.END_DOCUMENT) {       //문단의 마지막까지 반복
                    if (eventType == XmlPullParser.START_DOCUMENT) {    //문단의 시작
                        ;
                    } else if (eventType == XmlPullParser.START_TAG) {  //시작 태그일 때
                        String tag_name = xmlpp.getName();              //태그 이름값

                        switch (tag_name) {

                            case "headerCd":                            //태그이름이 headerCd이면
                                bus_headerCd = true;
                                break;
                            case "adirection":
                                bus_adirection = true;
                                break;
                            case "arrmsg1":
                                bus_arrmsg1 = true;
                                break;
                            case "arrmsg2":
                                bus_arrmsg2 = true;
                                break;
                            case "rtNm":
                                bus_rtNm = true;
                                break;
                            case "term":
                                bus_term = true;
                                break;
                        }

                    } else if (eventType == XmlPullParser.TEXT) { //내용 일때
                        if (bus_headerCd) {
                            headerCd = xmlpp.getText();
                            bus_headerCd = false;
                        }

                        if (headerCd.equals("0")) {
                            if (bus_adirection) {       //필요한 내용을 가져온다
                                saveCount2++;
                                adirection = xmlpp.getText();
                                infoDirection = adirection;
                                bus_adirection = false;
                            }
                            if (bus_arrmsg1) {
                                arrmsg1 = xmlpp.getText();
                                infoArrTime1 = arrmsg1;
                                bus_arrmsg1 = false;
                            }
                            if (bus_arrmsg2) {
                                arrmsg2 = xmlpp.getText();
                                infoArrTime2 = arrmsg2;
                                bus_arrmsg2 = false;
                            }
                            if (bus_rtNm) {
                                rtNm = xmlpp.getText();
                                infoBusNumber = rtNm;
                                bus_rtNm = false;
                            }
                            if (bus_term) {
                                term = xmlpp.getText();
                                infoInterval = term;
                                makeLayout();           //검색 결과를 출력할 버튼과 택스트뷰 만드는 함수
                                bus_term = false;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlpp.next();
                }
            } catch (Exception e) {
                textView.setText(e.getMessage());
            }


        }

        public String downloadByUrl(String myurl) throws IOException {

            HttpURLConnection conn = null;
            try {
                URL url = new URL(myurl);
                conn = (HttpURLConnection) url.openConnection();

                BufferedInputStream buffer = new BufferedInputStream(conn.getInputStream());

                BufferedReader buffer_reader = new BufferedReader(new InputStreamReader(buffer, "utf-8"));

                String line = null;
                getData = "";
                while ((line = buffer_reader.readLine()) != null) {
                    getData += line;

                }
                return getData;
            } finally {
                conn.disconnect();
            }
        }

    }



    public void searchCurrentBus(View v) {  //검색 버튼을 누르면 호출될 함수

        textView.setText("");           //오류 및 관련 내용을 출력하기 위한 택스트 뷰 비우기
        screen.removeAllViews();        //검색 결과를 출력할 리니어 레이아웃 비우기

        list.setVisibility(View.GONE);  //검색 기록 리스트 뷰 비활성화
        textView.setVisibility(View.VISIBLE);   //오류 및 관련 내용을 출력하기 위한 택스트 뷰 활성화
        screen.setVisibility(View.VISIBLE);     //검색 결과를 출력할 리니어 레이아웃 활성화
        saveList.add(stName.getText().toString());  //검색창에 있는 문자열을 리스트에 추가
        adapter.notifyDataSetChanged();

        String serviceUrl2 = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByName";
        String serviceKey2 = "WNYOOnmIyX4ZIcLeUFZ2M%2BwJ7gZAYGX9%2B2TvPXcD3JJDE%2B57d2BzPnK83dEBcwp1%2Fc5FKqkkEPwSaOYq9Xx4og%3D%3D";
        String strSrch2 = stName.getText().toString(); //"http://ws.bus.go.kr/api/rest/stationinfo/getStationByName"은 한글을 UrlEncoding해야함

        try {
            urlEncode = URLEncoder.encode(strSrch2, "utf-8");   //한글로 입력된 strSrch2를 UrlEncoding함
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        String strUrl2 = serviceUrl2 + "?ServiceKey=" + serviceKey2 + "&stSrch=" + urlEncode;   //api를 활용하기 위한 주소

        DownloadWebContent2 dwc2 = new DownloadWebContent2();
        dwc2.execute(strUrl2);

    }

    public void makeLine(){
        TextView lineText = new TextView(this);           //검색결과 출력용 택스트 뷰
        lineText.setLayoutParams(pm);                     //택스트뷰 파라미터 설정
        lineText.setGravity(Gravity.CENTER);              //중앙 정렬
        lineText.setText("+++++++++++++++++++++++++++++++++++++++++++++++++");
        screen.addView(lineText);
    }

    public void makeLayout(){
        String direction = infoDirection;   //방향
        String arrTime1 = infoArrTime1;     //다음차 시간
        String arrTime2 = infoArrTime2;     //다다음차 시간
        String busNumber = infoBusNumber;   //버스번호
        String interval = infoInterval;     //배차간격

        LinearLayout nwLayout = new LinearLayout(this); //버튼과 택스트뷰를 넣기 위한 새로운 리니어 레이아웃
        nwLayout.setOrientation(LinearLayout.VERTICAL); //리니어 레이아웃 정렬 방향
        nwLayout.setLayoutParams(layoutParam);          //리니어 레이아웃 파라미터 설정
        screen.addView(nwLayout);                       //리니어 레이아웃을 screen(리니어 레이아웃) 추가

        Button nwBut = new Button(this);                //검색결과 출력용 버튼
        nwBut.setLayoutParams(pm);                      //버튼 파라미터 설정
        nwBut.setBackgroundColor(Color.WHITE);          //버튼 색

        TextView nwText = new TextView(this);           //검색결과 출력용 택스트 뷰
        nwText.setLayoutParams(pm);                     //택스트뷰 파라미터 설정
        nwText.setGravity(Gravity.CENTER);              //중앙 정렬
        nwText.setText("");                             //초기 문자 설정
        nwText.setVisibility(View.GONE);                //초기 택스트뷰 비활성화

        nwBut.setText(busNumber + "\t" + direction + "행"); //버튼의 내용
        nwLayout.addView(nwBut);                            //리니어 레이아웃에 버튼 추가
        nwLayout.addView(nwText);                           //리니어 레이아웃에 택스트뷰 추가

        nwBut.setOnClickListener(new View.OnClickListener() {   //버튼 클릭시 함수
            @Override
            public void onClick(View view) {
                if(nwText.getVisibility() == View.VISIBLE){ //택스트뷰가 활성화 되어있으면
                    nwText.setText("");                     //택스트뷰 내용 초기화
                    nwText.setVisibility(View.GONE);        //택스트뷰 비활성화
                }
                else {                                      //택스트뷰가 비활성화 되어있으면
                    nwText.setVisibility(View.VISIBLE);     //택스트뷰 활성화
                    nwText.append("\n다음차: " + arrTime1 + "\n" + "다다음차: " + arrTime2 + "\n" + "배차간격: " + interval + "분" + "\n");   //내용
                }
            }
        });
    }

    public void searchList(View v){ //검색창을 누르면 호출되는 함수
        list.setVisibility(View.VISIBLE);   //리스트뷰 활성화
        textView.setVisibility(View.GONE);  //택스트뷰 비활성화
        screen.setVisibility(View.GONE);    //검색 결과용 리니어 레이아웃 비활성화
    }

}