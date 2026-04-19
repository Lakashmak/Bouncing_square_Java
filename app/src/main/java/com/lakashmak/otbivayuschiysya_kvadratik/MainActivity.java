package com.lakashmak.otbivayuschiysya_kvadratik;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.graphics.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
	//ГЛОБАЛЬНЫЕ ПЕРЕМЕННЫЕ:
	ImageView pictureBox1;
	TextView label1; //табло с FPS
	Button button1; //кнопка шага для отладки
	EditText textBox1; //текстовое поле для кастомных логов для отладки
	CTimer timer1;
	Canvas gr;
	Bitmap bitmap;
	//Paint pen;
	ArrayList<Kvadratik> kvadratiks; //массив квадратиков
	int selectedK = -1; //ID выделенного нажатием квадратика
	//double x = 50, y = 50, w = 100, h = 100; //старые данные первого квадратика, сейчас они в onCreate()
	//double vx = 2, vy = 3;
	int t = 0; //счётчик кадров
	boolean orientation; //ориентация экрана (горизонтально ли он повёрнут)
	boolean firstCall = true; //проверка на первый вход в pictureBox1_SizeChanged() что-бы скипнуть обработку при изменении с нулевого размера при инициализации pictureBox1
	boolean pause = false; //остановка таймера при отладке
	String message = "", message1 = ""; //сообщения для кастомных логов в режиме отладки
	boolean debuging = false; //режим отладки
	boolean dragging = false; //режим перетаскивания квадратиков по экрану
    //float touchX = 0, touchY = 0, newTouchX = 0, newTouchY = 0; //старые координаты нажатия
	long newTime, lastTime; //значение реального времени для нормализации движения при разных FPS
	double fps = 0;
	double deletimer = 0; //счётчик времени для удаления квадратика двойным/тройным нажатием
	//int countTouches = 0;
	
	//void timer1Start() { try { timer1.cancel(); } catch(Exception e) {} timer1 = new Timer(); timer1.scheduleAtFixedRate(new TimerTask() { @Override public void run() {timer1_Tick();} }, 0, 1); }
	//void timer1Stop() { timer1.cancel(); } //старые функции для удобства работы с таймером
	
//------------------------- РЕЖИМЫ ОКНА --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
    @Override //создание окна приложения:
    protected void onCreate(Bundle savedInstanceState) {
    	labelWrite("вход onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //инициализация элементов управления:
        pictureBox1 = findViewById(R.id.pictureBox1);
        label1 = findViewById(R.id.label1);
        button1 = findViewById(R.id.button1);
        textBox1 = findViewById(R.id.textBox1);
        lastTime = System.currentTimeMillis();
        
        //инициализация событий:
        pictureBox1.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { @Override public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) { pictureBox1_SizeChanged(); } });
        pictureBox1.setOnTouchListener(new View.OnTouchListener() { @Override public boolean onTouch(View v, MotionEvent event) { 
        	if(event.getActionMasked() == MotionEvent.ACTION_DOWN || event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) pictureBox1_TouchDown(event); 
            if(event.getActionMasked() == MotionEvent.ACTION_MOVE /*|| event.getAction() == MotionEvent.ACTION_POINTER_MOVE*/) pictureBox1_TouchMove(event); 
            if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) pictureBox1_TouchUp(event); 
            return true;
        } });
        button1.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { button1_Click(); } });
        if(!debuging) {
        	button1.setVisibility(View.GONE);
            textBox1.setVisibility(View.GONE);
        }
        
        //подготовка к запуску:
        if(pictureBox1.getWidth() != 0 && pictureBox1.getHeight() != 0) {
            bitmap = Bitmap.createBitmap(pictureBox1.getWidth(), pictureBox1.getHeight(), Bitmap.Config.ARGB_8888);
            gr = new Canvas(bitmap); 
        }
        timer1 = new CTimer(1) { @Override public void Tick() { super.Tick(); timer1_Tick(); } };
        //pen = new Paint();
        //pen.setColor(Color.RED);
        kvadratiks = new ArrayList<>();
        kvadratiks.add(new Kvadratik(50, 50, 100, 100, 2, 3, Color.RED));
        timer1.Start();
        labelWrite("выход onCreate");
    }
    
    @Override //выход в фоновый режим:
    protected void onPause() {
    	labelWrite("вход onPause");
    	super.onPause();
        //timer1Stop();
        fps = 0;
        labelWrite("выход onPause");
    }

    @Override //возвращение в окно из фонового режима:
    protected void onResume() {
    	labelWrite("вход onResume");
        super.onResume();
        //timer1Start();
        pictureBox1 = findViewById(R.id.pictureBox1);
        if(pictureBox1.getWidth() != 0 && pictureBox1.getHeight() != 0) {
            bitmap = Bitmap.createBitmap(pictureBox1.getWidth(), pictureBox1.getHeight(), Bitmap.Config.ARGB_8888);
            gr = new Canvas(bitmap); 
            //Draw();
        }
        labelWrite("выход onResume");
    }
    
//-------------------- ОБРАБОТЧИКИ СОБЫТИЙ ------------------------------------------------------------------------------------------------------------------------------------------
    
    //изменение размеров окна (поворот экран):
    void pictureBox1_SizeChanged() {
    	labelWrite("вход pictureBox1_SizeChanged");
    	if(firstCall) { orientation = pictureBox1.getWidth() > pictureBox1.getHeight(); firstCall = false; return;}
    	if(orientation && pictureBox1.getWidth() < pictureBox1.getHeight()) {
    	    for(int i = 0; i < kvadratiks.size(); i++) {
    	        Kvadratik k = kvadratiks.get(i);
    	        double x1 = pictureBox1.getWidth()-k.y, y1 = k.x, vx1 = -k.vy, vy1 = k.vx, w1 = k.h, h1 = k.w;
                k.x = x1; k.y = y1; k.vx = vx1; k.vy = vy1; k.w = w1; k.h = h1;
            }
            orientation = !orientation;
    	} else if(!orientation && pictureBox1.getWidth() > pictureBox1.getHeight()) {
    	    for(int i = 0; i < kvadratiks.size(); i++) {
    	        Kvadratik k = kvadratiks.get(i);
    	        double x1 = k.y, y1 = pictureBox1.getHeight()-k.x, vx1 = k.vy, vy1 = -k.vx, w1 = k.h, h1 = k.w;
                k.x = x1; k.y = y1; k.vx = vx1; k.vy = vy1; k.w = w1; k.h = h1;
            }
            orientation = !orientation;
    	}
        if(pictureBox1.getWidth() != 0 && pictureBox1.getHeight() != 0) {
            bitmap = Bitmap.createBitmap(pictureBox1.getWidth(), pictureBox1.getHeight(), Bitmap.Config.ARGB_8888);
            gr = new Canvas(bitmap); 
            //Draw();
        }
        labelWrite("выход pictureBox1_SizeChanged");
    }
    
    //начало нажатия (опускание пальца на экран):
    void pictureBox1_TouchDown(MotionEvent e) {
    	labelWrite("вход pictureBox1_TouchDown");
        int touchID = e.getPointerId(e.getActionIndex());
        float touchX = e.getX(e.getActionIndex());
        float touchY = e.getY(e.getActionIndex());
        
        if(!Touch.inStock(touchID)) Touch.add(new Touch(touchID, touchX, touchY, touchX, touchY));
        else {
        	Touch touch = Touch.get(touchID);
            touch = new Touch(touchID, touchX, touchY, touchX, touchY);
        }
        
        for(int i = 0; i < kvadratiks.size(); i++) {
        	Kvadratik k = kvadratiks.get(i);
            if(touchX > k.x - Math.abs(k.w)/2 && touchX < k.x + Math.abs(k.w)/2 && touchY > k.y - Math.abs(k.h)/2 && touchY < k.y + Math.abs(k.h)/2) {
            	if(!k.dragging) {
            	    if(!dragging && i == selectedK) deletimer += 500;
                    dragging = true;
            	    k.dragging = true;
                    k.touchID = touchID;
                    selectedK = i;
                } else if(!k.stretching) {
                	k.stretching = true;
                    k.touchID2 = touchID;
                }
            }
        }
        
        if(!dragging) {
        	kvadratiks.add(new Kvadratik(touchX, touchY, 100, 100, true, touchID));
            dragging = true;
        }
        
        //newTouchX = touchX;
        //newTouchY = touchY;
        labelWrite("выход pictureBox1_TouchDown");
    }
    
    //движение пальца по экрану:
    void pictureBox1_TouchMove(MotionEvent e) {
    	labelWrite("вход pictureBox1_TouchMove");
    	for(int i = 0; i < e.getPointerCount(); i++) {
    	    int touchID = e.getPointerId(i);
            if(Touch.inStock(touchID)) {
                Touch touch = Touch.get(touchID);
    	        touch.newX = e.getX(i);
                touch.newY = e.getY(i);
            } //else Touch.add(new Touch(touchID, e.getX(i), e.getY(i), e.getX(i), e.getY(i)));
        }
        labelWrite("выход pictureBox1_TouchMove");
    }
    
    //конец нажатия (поднятие пальца от экрана):
    void pictureBox1_TouchUp(MotionEvent e) {
    	labelWrite("вход pictureBox1_TouchUp");
    	int touchID = e.getPointerId(e.getActionIndex());
        for(int i = 0; i < kvadratiks.size(); i++) {
    	    Kvadratik k = kvadratiks.get(i);
            if(k.stretching) {
            	if(k.touchID == touchID) {
            	    k.touchID = k.touchID2;
                    k.touchID2 = -1;
                    k.stretching = false;
                }
                if(k.touchID2 == touchID) {
                	k.touchID2 = -1;
            	    k.stretching = false;
                }
            } else {
        	    if(k.touchID == touchID) {
        	        k.touchID = -1;
                    k.dragging = false;
                }
            }
        }
        Touch.remove(touchID);
        if(e.getPointerCount() <= 1) {
        	for(int i = 0; i < kvadratiks.size(); i++) {
    	        Kvadratik k = kvadratiks.get(i);
                k.touchID = -1;
                k.touchID2 = -1;
                k.stretching = false;
                k.dragging = false;
            }
            dragging = false;
            Touch.clear();
        }
        labelWrite("выход pictureBox1_TouchUp");
    }
    
    //кнопка шага для отладки
    void button1_Click() {
    	pause = false;
        message = message1;
        message1 = "";
        timer1.Start();
    }
    
    void timer1_Tick() {
    	if(pictureBox1.getWidth() == 0 || pictureBox1.getHeight() == 0) return;
    	labelWrite("вход timer1_Tick");
        newTime = System.currentTimeMillis();
        if(deletimer != 0) {
        	if(deletimer > 500) {
        	    kvadratiks.remove(selectedK);
                selectedK = -1;
                deletimer = 0;
            }
        	deletimer -= (newTime - lastTime);
            if(deletimer < 0) deletimer = 0;
        }
    	Move();
    	Draw();
        try {
            double newFPS = 1000.0 / (newTime - lastTime);
            if(fps == 0) fps = newFPS;
            fps = (fps*99.0 + newFPS) / 100.0;
            /*if(!debuging)*/ label1.setText("FPS: " + Math.round(fps) /*+ "\nX: " + x + "\nY: " + y*/);
            lastTime = newTime;
        } catch(Exception e) {}
        t++;
        labelWrite("выход timer1_Tick");
        Stop(); return;
    }
    
//------------------------- ФУНКЦИИ РАСЧЁТОВ ----------------------------------------------------------------------------------------------------------------------------------
    
    //ФУНКЦИИ ДЛЯ ОТЛАДКИ:
    void labelWrite() { if(debuging) { textBox1.setText("T: " + (t) /*+ "\nX: " + x + "\nY: " + y*/ + "\n" + message); } }
    void labelWrite(String funcLoc) { if(debuging) {
    	String str = "\nсейчас выполняется " + funcLoc + ":" + " \n    Touch = [";
        for(int i = 0; i < Touch.size(); i++) {
        	Touch touch = Touch.getRealId(i);
        	str += "\n        [" + touch.id + "; " + touch.X + "; " + touch.Y + "; " + touch.newX + "; " + touch.newY + "];";
        }
        str += "\n    ]";
    	message += str;
        try{ labelWrite(); } catch(Exception e) { 
        	message1 += "\nне удалось отобразить:" + str;
        } 
    }}
    
    void Stop() { if(debuging) { pause = true; timer1.Stop(); /*try { while(pause) Thread.sleep(10); } catch(Exception e) {}*/ } }
    
    //ОТРИСОВКА НА ЭКРАН
    //void Draw() { runOnUiThread(() -> { Draw(true); }); }
    void Draw(/*boolean fake*/) {
    	labelWrite("вход Draw");
        //try { pictureBox1.getHolder().unlockCanvasAndPost(gr); } catch(Exception e) {}
        try {
        	//canvas = pictureBox1.getHolder().lockCanvas();
        	if (gr == null) {
        	    bitmap = Bitmap.createBitmap(pictureBox1.getWidth(), pictureBox1.getHeight(), Bitmap.Config.ARGB_8888);
                gr = new Canvas(bitmap);
            }
        	if(pictureBox1.getWidth() != 0 && pictureBox1.getHeight() != 0) {
        	    gr.drawColor(Color.WHITE);
                for(int i = 0; i < kvadratiks.size(); i++) {
                	Kvadratik k = kvadratiks.get(i);
                    gr.drawRect((float)(k.x-k.w/2), (float)(k.y-k.h/2), (float)(k.x+k.w/2), (float)(k.y+k.h/2), k.pen);
                }
                pictureBox1.setImageBitmap(bitmap.copy(Bitmap.Config.ARGB_8888, false)); //getHolder().unlockCanvasAndPost(gr);
            }
        } catch(Exception e) {
        	/*pictureBox1 = findViewById(R.id.pictureBox1);*/ 
            //pictureBox1.getHolder().addCallback(new SurfaceHolder.Callback() { public void surfaceCreated(SurfaceHolder holder) {} public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {} public void surfaceDestroyed(SurfaceHolder holder) {} });
            /*if(pictureBox1.getWidth() != 0 && pictureBox1.getHeight() != 0) {
                bitmap = Bitmap.createBitmap(pictureBox1.getWidth(), pictureBox1.getHeight(), Bitmap.Config.ARGB_8888);
                gr = new Canvas(bitmap);
            }*/
        }
        labelWrite("выход Draw");
    }
    
    //РАСЧЁТ ВСЕГО ДВИЖЕНИЯ КВАДРАТИКОВ:
    void Move() {
    	labelWrite("вход Move");
        
        //перетаскивание с помощью подстройки скорости:
        if(dragging) {
        	for(int i = 0; i < kvadratiks.size(); i++) {
        	    Kvadratik k = kvadratiks.get(i);
                if(k.stretching) { //растягивание квадратика
                	Touch touch1 = Touch.get(k.touchID);
                    Touch touch2 = Touch.get(k.touchID2);
                    k.w *= (touch1.newX - touch2.newX) / (touch1.X - touch2.X);
                    k.h *= (touch1.newY - touch2.newY) / (touch1.Y - touch2.Y);
                    k.vx = ((touch1.newX - touch1.X) + (touch2.newX - touch2.X)) / 2 / (newTime - lastTime) * (50.0 / 3.0);
                    k.vy = ((touch1.newY - touch1.Y) + (touch2.newY - touch2.Y)) / 2 / (newTime - lastTime) * (50.0 / 3.0);
                	touch1.X = touch1.newX;
                    touch1.Y = touch1.newY;
                    touch2.X = touch2.newX;
                    touch2.Y = touch2.newY;
                } else if(k.dragging) { //перетаскивание квадратика
                	Touch touch = Touch.get(k.touchID);
        	        k.vx = Math.signum(k.w * k.h) * (touch.newX - touch.X) / (newTime - lastTime) * (50.0 / 3.0);
                    k.vy = Math.signum(k.w * k.h) * (touch.newY - touch.Y) / (newTime - lastTime) * (50.0 / 3.0);
                    touch.X = touch.newX;
                    touch.Y = touch.newY;
                }
            }
        }
        
        //шаг движения квадратиков:
        for(int i = 0; i < kvadratiks.size(); i++) {
        	Kvadratik k = kvadratiks.get(i);
    	    k.x += Math.signum(k.w*k.h) * k.vx * (newTime - lastTime) / (50.0 / 3.0);
    	    k.y += Math.signum(k.w*k.h) * k.vy * (newTime - lastTime) / (50.0 / 3.0);
        }
        
        labelWrite("середина Move");
        
        //отскок квадратиков друг от друга:
        for(int i = 0; i < kvadratiks.size(); i++) for(int j = i + 1; j < kvadratiks.size(); j++) {
        	Kvadratik k1 = kvadratiks.get(i); //k1.x k1.y k1.vx k1.vy k1.w k1.h
            Kvadratik k2 = kvadratiks.get(j); //k2.x k2.y k2.vx k2.vy k2.w k2.h
            
            if(Math.abs(k1.x - k2.x) <= Math.abs(k1.w)/2 + Math.abs(k2.w)/2 && Math.abs(k1.y - k2.y) <= Math.abs(k1.h)/2 + Math.abs(k2.h)/2) {
                if(Math.abs(k1.x - k2.x) - (Math.abs(k1.w)/2 + Math.abs(k2.w)/2) > Math.abs(k1.y - k2.y) - (Math.abs(k1.h)/2 + Math.abs(k2.h)/2)) {
                    double newVx1 = -k1.vx + 2*(k1.vx*k1.w*k1.h + k2.vx*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    double newVx2 = -k2.vx + 2*(k1.vx*k1.w*k1.h + k2.vx*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    if(Math.abs((k1.x + newVx1 * Math.signum(k1.w*k1.h)) - (k2.x + newVx2 * Math.signum(k2.w*k2.h))) > Math.abs(k1.x - k2.x)) {
                        k1.vx = newVx1;
                        k2.vx = newVx2;
                    }
                    if(k1.x != k2.x) {
                        double newX1 = k1.x + 2*Math.signum(k1.x - k2.x)*((Math.abs(k1.w)/2 + Math.abs(k2.w)/2) - Math.abs(k1.x - k2.x)) * (k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                        double newX2 = k2.x + 2*Math.signum(k2.x - k1.x)*((Math.abs(k1.w)/2 + Math.abs(k2.w)/2) - Math.abs(k1.x - k2.x)) * (k1.w*k1.h) / (k1.w*k1.h + k2.w*k2.h);
                        if(Math.abs(newX1 - newX2) > Math.abs(k1.x - k2.x)) {
                            k1.x = newX1;
                            k2.x = newX2;
                        }
                    }
                } else if(Math.abs(k1.x - k2.x) - (Math.abs(k1.w)/2 + Math.abs(k2.w)/2) < Math.abs(k1.y - k2.y) - (Math.abs(k1.h)/2 + Math.abs(k2.h)/2)) {
                    double newVy1 = -k1.vy + 2*(k1.vy*k1.w*k1.h + k2.vy*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    double newVy2 = -k2.vy + 2*(k1.vy*k1.w*k1.h + k2.vy*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    if(Math.abs((k1.y + newVy1 * Math.signum(k1.w*k1.h)) - (k2.y + newVy2 * Math.signum(k2.w*k2.h))) > Math.abs(k1.y - k2.y)) {
                        k1.vy = newVy1;
                        k2.vy = newVy2;
                    }
                    if(k1.y != k2.y) {
                        double newY1 = k1.y + 2*Math.signum(k1.y - k2.y)*((Math.abs(k1.h)/2 + Math.abs(k2.h)/2) - Math.abs(k1.y - k2.y)) * (k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                        double newY2 = k2.y + 2*Math.signum(k2.y - k1.y)*((Math.abs(k1.h)/2 + Math.abs(k2.h)/2) - Math.abs(k1.y - k2.y)) * (k1.w*k1.h) / (k1.w*k1.h + k2.w*k2.h);
                        if(Math.abs(newY1 - newY2) > Math.abs(k1.y - k2.y)) {
                            k1.y = newY1;
                            k2.y = newY2;
                        }
                    }
                } else {
                    double newVx1 = -k1.vx + 2*(k1.vx*k1.w*k1.h + k2.vx*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    double newVx2 = -k2.vx + 2*(k1.vx*k1.w*k1.h + k2.vx*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    double newVy1 = -k1.vy + 2*(k1.vy*k1.w*k1.h + k2.vy*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    double newVy2 = -k2.vy + 2*(k1.vy*k1.w*k1.h + k2.vy*k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                    if(Math.abs((k1.x + newVx1 * Math.signum(k1.w*k1.h)) - (k2.x + newVx2 * Math.signum(k2.w*k2.h))) > Math.abs(k1.x - k2.x)) {
                        k1.vx = newVx1;
                        k2.vx = newVx2;
                    }
                    if(Math.abs((k1.y + newVy1 * Math.signum(k1.w*k1.h)) - (k2.y + newVy2 * Math.signum(k2.w*k2.h))) > Math.abs(k1.y - k2.y)) {
                        k1.vy = newVy1;
                        k2.vy = newVy2;
                    }
                    if(k1.x != k2.x) {
                        double newX1 = k1.x + 2*Math.signum(k1.x - k2.x)*((Math.abs(k1.w)/2 + Math.abs(k2.w)/2) - Math.abs(k1.x - k2.x)) * (k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                        double newX2 = k2.x + 2*Math.signum(k2.x - k1.x)*((Math.abs(k1.w)/2 + Math.abs(k2.w)/2) - Math.abs(k1.x - k2.x)) * (k1.w*k1.h) / (k1.w*k1.h + k2.w*k2.h);
                        if(Math.abs(newX1 - newX2) > Math.abs(k1.x - k2.x)) {
                            k1.x = newX1;
                            k2.x = newX2;
                        }
                    }
                    if(k1.y != k2.y) {
                        double newY1 = k1.y + 2*Math.signum(k1.y - k2.y)*((Math.abs(k1.h)/2 + Math.abs(k2.h)/2) - Math.abs(k1.y - k2.y)) * (k2.w*k2.h) / (k1.w*k1.h + k2.w*k2.h);
                        double newY2 = k2.y + 2*Math.signum(k2.y - k1.y)*((Math.abs(k1.h)/2 + Math.abs(k2.h)/2) - Math.abs(k1.y - k2.y)) * (k1.w*k1.h) / (k1.w*k1.h + k2.w*k2.h);
                        if(Math.abs(newY1 - newY2) > Math.abs(k1.y - k2.y)) {
                            k1.y = newY1;
                            k2.y = newY2;
                        }
                    }
                }
            }
        }
        
        //отскок квадратиков от стен:
        for(int i = 0; i < kvadratiks.size(); i++) {
        	Kvadratik k = kvadratiks.get(i);
            if(k.x <= Math.abs(k.w)/2) {
            	k.vx = Math.abs(k.vx) * Math.signum(k.w*k.h);
                k.x += 2*(Math.abs(k.w)/2 - k.x);
            }
            if(k.x >= pictureBox1.getWidth() - Math.abs(k.w)/2) {
            	k.vx = -Math.abs(k.vx) * Math.signum(k.w*k.h);
                k.x -= 2*(k.x - (pictureBox1.getWidth() - Math.abs(k.w)/2));
            }
            if(k.y <= Math.abs(k.h)/2) {
            	k.vy = Math.abs(k.vy) * Math.signum(k.w*k.h);
                k.y += 2*(Math.abs(k.h)/2 - k.y);
            }
            if(k.y >= pictureBox1.getHeight() - Math.abs(k.h)/2) {
            	k.vy = -Math.abs(k.vy) * Math.signum(k.w*k.h);
                k.y -= 2*(k.y - (pictureBox1.getHeight() - Math.abs(k.h)/2));
            }
        }
        
        labelWrite("выход Move");
    }
}

//------------------------ КАСТОМНЫЕ КЛАССЫ -------------------------------------------------------------------------------------------------------------------------

/*abstract*/ class CTimer {
	protected android.os.Handler handler;
	protected Runnable gameLoop;
	protected int interval;
	
	public CTimer() { this(16); }
	public CTimer(int interval) {
		if(interval > 0) this.interval = interval;
		else interval = 1;
		handler = new android.os.Handler();
		gameLoop = new Runnable() { @Override public void run() { Tick(); /*nextTick();*/ } };
	}
	
	//public void nextTick() { handler.postDelayed(gameLoop, this.interval); }
	/*abstract*/ public void Tick() { handler.postDelayed(gameLoop, this.interval); }
	public void Start() { handler.post(gameLoop); }
	public void Start(int delay) { handler.postDelayed(gameLoop, delay); }
	public void Stop() { handler.removeCallbacks(gameLoop); }
	public void setInterval(int interval) { if(interval > 0) this.interval = interval; }
	public int getInterval() { return this.interval; }
}

class Touch {
	static java.util.ArrayList<Touch> list = new java.util.ArrayList<Touch>();
	
	public int id;
	public float X;
	public float Y;
	public float newX;
	public float newY;
	
	public Touch(int id, float X, float Y, float newX, float newY) {
		this.id = id;
		this.X = X;
		this.Y = Y;
		this.newX = newX;
		this.newY = newY;
	}
	
	public static void add(int id, float X, float Y, float newX, float newY) {
		list.add(new Touch(id, X, Y, newX, newY));
	}
	
	public static void add(Touch touch) {
		list.add(touch);
	}
	
	public static Touch get(int id) {
		for(int i = 0; i < list.size(); i++) if(list.get(i).id == id) return list.get(i);
		return null;
	}
	
	public static void remove(int id) {
		for(int i = 0; i < list.size(); i++) if(list.get(i).id == id) {
			list.remove(i);
			return;
		}
	}
	
	public static void remove(Touch touch) {
		for(int i = 0; i < list.size(); i++) if(list.get(i) == touch) {
			list.remove(i);
			return;
		}
	}
	
	public static int size() {
		return list.size();
	}
	
	public static Touch getRealId(int i) {
		return list.get(i);
	}
	
	public static boolean inStock(int id) {
		for(int i = 0; i < list.size(); i++) if(list.get(i).id == id) return true;
		return false;
	}
	
	public static boolean inStock(Touch touch) {
		for(int i = 0; i < list.size(); i++) if(list.get(i) == touch) return true;
		return false;
	}
	
	public static void clear() {
		list.clear();
	}
}