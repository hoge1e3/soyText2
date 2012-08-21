package jp.tonyu.debug;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LogWindow extends Frame {
	TextArea text;
	public LogWindow(final Runnable onClose) {
		setLayout(new BorderLayout());
		text=new TextArea();
		text.setFont(new Font("Monospaced",0,11));
		add(text,BorderLayout.CENTER);
		setSize(500,300);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				setVisible(false);
				onClose.run();
			}
		});
	}
	public void println(String msg) {
		text.append(msg+"\n");
	}
    public void print(String msg) {
        text.append(msg);
    }
}
