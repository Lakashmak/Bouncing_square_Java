package com.lakashmak.otbivayuschiysya_kvadratik;

import java.util.*;
import android.graphics.*;

public class Kvadratik {
	static Random rand = new Random();
	public double w, h;
	public double x, y;
	public double vx, vy;
	public Paint pen;
	public boolean dragging;
	public int touchID;
	public boolean stretching;
	public int touchID2;
	
	public Kvadratik(double x, double y, double w, double h) { this(x, y, w, h, 0, 0, rand.nextInt(0x00FFFFFF) | 0xFF000000, false); } //int color = rand.nextInt(16777215) + 4278190080;
	public Kvadratik(double x, double y, double w, double h, int color) { this(x, y, w, h, 0, 0, color, false); }
	public Kvadratik(double x, double y, double w, double h, boolean dragging, int touchID) { this(x, y, w, h, 0, 0, rand.nextInt(0x00FFFFFF) | 0xFF000000, dragging, touchID); }
	public Kvadratik(double x, double y, double w, double h, int color, boolean dragging, int touchID) { this(x, y, w, h, 0, 0, color, dragging, touchID); }
	public Kvadratik(double x, double y, double w, double h, double vx, double vy) { this(x, y, w, h, vx, vy, rand.nextInt(0x00FFFFFF) | 0xFF000000, false); }
	public Kvadratik(double x, double y, double w, double h, double vx, double vy, int color) { this(x, y, w, h, vx, vy, color, false); }
	public Kvadratik(double x, double y, double w, double h, double vx, double vy, boolean dragging, int touchID) { this(x, y, w, h, vx, vy, rand.nextInt(0x00FFFFFF) | 0xFF000000, dragging, touchID); }
	public Kvadratik(double x, double y, double w, double h, double vx, double vy, int color, boolean dragging, int touchID) { this(x, y, w, h, vx, vy, color, dragging); this.touchID = touchID; this.dragging = dragging; }
	public Kvadratik(double x, double y, double w, double h, double vx, double vy, int color, boolean dragging) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.vx = vx;
		this.vy = vy;
		pen = new Paint();
        pen.setColor(color);
		this.dragging = false;
		this.stretching = false;
		this.touchID = -1;
		this.touchID2 = -1;
	}
}