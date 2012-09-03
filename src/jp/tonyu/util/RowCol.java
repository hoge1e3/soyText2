package jp.tonyu.util;

public class RowCol {
	private CharSequence src;
	private int row,col;
	private int byteCount;
	public int setRowCol(int r, int c) {
		row=0;
		col=0;
		for (byteCount=0 ; byteCount<src.length() ; byteCount++) {
			char ch=src.charAt(byteCount);
			if (row>=r && col>=c) { return byteCount;}
			if (ch=='\n') {
				if (row>=r) { return byteCount; }
				row++; col=0;
			} else {
				col++;
			}
		}
		return byteCount;
	}
	public void setByteCount(int cnt) {
		row=0;
		col=0;
		if (cnt>=src.length()) cnt=src.length();
		for (byteCount=0 ; byteCount<cnt ; byteCount++) {
			char ch=src.charAt(byteCount);
			if (ch=='\n') {
				row++; col=0;
			} else {
				col++;
			}
		}
	}

	public CharSequence getSrc() {
		return src;
	}
	public int getRow() {
		return row;
	}
	public int getCol() {
		return col;
	}
	public int getByteCount() {
		return byteCount;
	}
	public RowCol(CharSequence src) {
		super();
		this.src = src;
	}
	@Override
	public String toString() {
		return "RowCol [byteCount=" + byteCount + ", col=" + col + ", row="
				+ row + ", src=" + src + "]";
	}
}
