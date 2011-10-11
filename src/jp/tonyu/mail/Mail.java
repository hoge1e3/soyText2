package jp.tonyu.mail;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

import org.xbill.DNS.MXRecord;
// from http://www.tenj.jp/modules/smartsection/print.php?itemid=73

public class Mail implements Wrappable {
	String from; 
	String to;
	String subject;
	String body;
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void send() throws UnknownHostException {
		try {
			String smtpServer=findMailServer(to);
			Log.d("smtpserver", smtpServer);
			Properties objPrp=new Properties();
			objPrp.put("mail.smtp.host",smtpServer); // SMTPサーバ名
			objPrp.put("mail.host",smtpServer); // 接続するホスト名
			// メールセッションを確立
			Session session=Session.getDefaultInstance(objPrp,null);
			// 送信メッセージを生成
			MimeMessage objMsg=new MimeMessage(session);
			// 送信先（TOのほか、CCやBCCも設定可能）
			objMsg.setRecipients(Message.RecipientType.TO,to);
			// Fromヘッダ
			InternetAddress objFrm=new InternetAddress(from,from);

			objMsg.setFrom(objFrm);
			// 件名
			objMsg.setSubject(subject,"ISO-2022-JP");

			// 本文
			objMsg.setText(body,"ISO-2022-JP");

			// メール送信
			Transport.send(objMsg); 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private static String findMailServer(String to) throws UnknownHostException {
		String []r =to.split("@");
		DNSFinder d = new DNSFinder(new String[]{"8.8.8.8"});
		for(MXRecord mr : d.findMXRecords(r[1])) {
			return mr.getTarget().toString();
		}
		InetAddress ip=d.findByName(r[1]);
		if (ip!=null) return r[1];
		throw new UnknownHostException();
	}
}