package org.ykmr.ui;

public interface MyButton{
	public static final int STATUS_UP = 0;
	public static final int STATUS_DOWN = 1;
	public boolean contains(int x, int y);
	public void setStatus(int status);
	public void onTap();
	public void setListener(ButtonTapListener l);

	public interface ButtonTapListener{
		public void onTap(int id);
	}
}
