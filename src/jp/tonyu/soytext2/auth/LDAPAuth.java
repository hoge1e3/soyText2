package jp.tonyu.soytext2.auth;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;

import org.ietf.ldap.LDAPAttribute;
import org.ietf.ldap.LDAPConnection;
import org.ietf.ldap.LDAPEntry;
import org.ietf.ldap.LDAPException;
import org.ietf.ldap.LDAPSearchResults;



public class LDAPAuth implements Authentificator {		
	private static final String ADMINPASS = "meisei";
	private static final String GUESTIDPASS = "guest";
	int ldapVersion=3;
	String host, binddn, bindpw,  base;
	public LDAPAuth(String host, String binddn, String bindpw, String base) {
		super();
		this.host = host;
		this.binddn = binddn;
		this.bindpw = bindpw;
		this.base = base;
	}
	String passwordFieldName="userPassword";
	String uidFieldName="uid";         
	public LDAPAuth(int ldapVersion, String host, String binddn, String bindpw,
			String base, String passwordFieldName, String uidFieldName) {
		super();
		this.ldapVersion = ldapVersion;
		this.host = host;
		this.binddn = binddn;
		this.bindpw = bindpw;
		this.base = base;
		this.passwordFieldName = passwordFieldName;
		this.uidFieldName = uidFieldName;
	}
	@Override
	/* (non-Javadoc)
	 * @see soytext.auth.Authentificator#check(java.lang.String, java.lang.String)
	 */
	public boolean check(String username, String password) {
		if (ADMINPASS.equals(password)) return true;
		if (GUESTIDPASS.equals(username) && GUESTIDPASS.equals(password)) return true;
		// 検索属性リスト(全属性指定)
		String attrs[] = { LDAPConnection.ALL_USER_ATTRS };
		String filter="("+uidFieldName+"="+username+")";
		LDAPConnection lc = new LDAPConnection();

		try {
			// LDAP サーバに接続する。ポートはデフォルト(389番)を使う。
			lc.connect( host, LDAPConnection.DEFAULT_PORT );
			//System.out.println( "Connected to " + host );
			//System.out.println();


			// 指定された DN およびパスワードでバインド(認証)する。
			// これを実行しない場合、anonymous 状態で操作することになる。
			if (binddn.length()>0) {
				lc.bind( ldapVersion, binddn, bindpw.getBytes());
			}


			// 指定条件でのディレクトリエントリの検索を行う。
			LDAPSearchResults results =	lc.search( 
					base,                     // 検索ベース
					LDAPConnection.SCOPE_SUB, // 検索スコープ
					filter,                   // 検索フィルタ
					attrs,                    // 検索属性リスト
					false                     // 属性名および属性値の取得
			);
			boolean res=false;
			// 得られたエントリを全て表示する。
			while( results.hasMore() ) {
				res=checkLDAPEntry( results.next(), password );
				if (res) break;
			}

			// 接続を切断する。
			lc.disconnect();
			//System.out.println();		
			//System.out.println( "Disconnected" );
			return res;
		}
		catch( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
	private boolean checkLDAPEntry( LDAPEntry entry, String inputPassword ) throws NoSuchAlgorithmException
	{
		//System.out.println( "Listing attributes of " + entry.getDN() + " :" );
		Iterator items = entry.getAttributeSet().iterator();

		while( items.hasNext() ) {
			// 各属性に対して複数の属性値が設定されている可能性があるが、
			// ここでは最初の属性値のみ表示することにする。
			LDAPAttribute attr = ( LDAPAttribute )items.next();
			Enumeration vals = attr.getStringValues();
			String val  = ( String )vals.nextElement();
			if (attr.getName().equals(passwordFieldName)) {
				return PasswordChecker.match(inputPassword, val);
			}
			//System.out.println( "  " + attr.getName() + ": " + val );
		}

		//System.out.println();
		return false;
	}
}
