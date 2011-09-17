package jp.tonyu.mail;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.xbill.DNS.Cache;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.xbill.DNS.Address;
import org.xbill.DNS.TXTRecord;

/**
 * from http://d.hatena.ne.jp/ttshrk/20110408/1302279815
 * @author tatsuhiro
 */
public class DNSFinder {
	//private static Log log = LogFactory.getLog(DnsFinder.class);

	// リゾルバ
	protected Resolver resolver;
	// DNSキャッシュ
	private Cache cache;
	//  キャッシュ最大サイズ
	private int maxCacheSize = 50000;
	private List<String> dnsServers = new ArrayList<String>();
	private Comparator mxComparator = new MXRecordComparator();

	/**
	 * 
	 * @param dnsHostName
	 * @throws java.lang.Exception
	 */
	public DNSFinder(String[] dnsHostName) throws UnknownHostException {
		if(dnsHostName != null && dnsHostName.length != 0) {
			for(String dhn : dnsHostName) {
				dnsServers.add(dhn);
			}
		} else {
			dnsServers.add("localhost");
		}

		try {
			resolver = new ExtendedResolver(dnsServers.toArray(new String[0]));
			Lookup.setDefaultResolver(resolver);
		} catch (UnknownHostException uhe) {
			throw uhe;
		}

		cache = new Cache(DClass.IN);
		cache.setMaxEntries(maxCacheSize);
		Lookup.setDefaultCache(cache, DClass.IN);
	}

	/**
	 * find mx records
	 * @param hostname
	 * @return
	 */
	public List<MXRecord> findMXRecords(String hostname) {
		List<MXRecord> ret = new ArrayList<MXRecord>();
		try {
			Record[] records = new Lookup(hostname, Type.MX).run();
			if(records == null || records.length == 0) {
				return ret;
			}

			for (Record r : records) {
				MXRecord mx = (MXRecord) r;
				ret.add(mx);
			}
			// MXレコードのpriorityに応じてソートする。
			// priorityが同じときはランダム
			Collections.sort(ret, mxComparator);
			return ret;
			
		} catch (TextParseException ex) {
			//log.info("MX find fault["+hostname+"]", ex);
			ex.printStackTrace();
		}
		return ret;
	}

	/**
	 * find name
	 * @param name
	 * @return
	 */
	public InetAddress findByName(String name) {
		try {
			InetAddress addr = Address.getByName(name);
			return addr;
		} catch (UnknownHostException ex) {
			//log.info("DNS find fault["+name+"]", ex);
			ex.printStackTrace();
		}

		return null;
	}

	/**
	 * find all name
	 * @param host
	 * @return
	 * @throws java.net.UnknownHostException
	 */
	public List<InetAddress> findAllByName(String name) {
		List<InetAddress> ret = new ArrayList<InetAddress>();
		try {
			InetAddress[] addrs =  Address.getAllByName(name);
			if(addrs == null || addrs.length == 0) {
				return ret;
			}

			for (InetAddress r : addrs) {
				ret.add(r);
			}
			return ret;

		} catch (UnknownHostException ex) {
//			log.info("name find fault["+name+"]", ex);
			ex.printStackTrace();
		}
		return ret;
	}

	/**
	 * find txt
	 * @param hostname
	 */
	public List<TXTRecord> findTxt(String hostname) {
		List<TXTRecord> ret = new ArrayList<TXTRecord>();
		try {
			Record[] records = new Lookup(hostname, Type.TXT).run();
			if(records == null || records.length == 0) {
				return ret;
			}

			for (Record r : records) {
				TXTRecord txt = (TXTRecord)r;
				ret.add(txt);
			}
			return ret;
		} catch (TextParseException ex) {
			//log.info("TXT find fault["+hostname+"]", ex);
			ex.printStackTrace();
		}

		return ret;
	}

	/**
	 * MXRecord sort comparator
	 * preferenceによってソートを行う。
	 * preferenceが同値のとき、その順序はランダムになる。
	 */
	private static class MXRecordComparator implements Comparator {

		private final static Random random = new Random();

		@Override
		public int compare(Object a, Object b) {
			int pa = ((MXRecord) a).getPriority();
			int pb = ((MXRecord) b).getPriority();
			return (pa == pb) ? (512 - random.nextInt(1024)) : pa - pb;
		}
	}
	
	public static void main(String[] args) throws UnknownHostException {
		DNSFinder instance = new DNSFinder(new String[]{"8.8.8.8"});

		for(MXRecord mr : instance.findMXRecords("docomo.ne.jp")) {
			System.out.println("additional name:" + mr.getAdditionalName());  // MXレコード
			System.out.println("name:" + mr.getName());
			System.out.println("priority:" + mr.getPriority());
		}

		// find mx and domain record
		String[] nl = new String[]{"docomo.ne.jp", "ezweb.ne.jp", "softbank.ne.jp"};
		for(String n : nl) {
			List<MXRecord> ml = instance.findMXRecords(n);
			System.out.println("mxrecord:" + ml);
			for(MXRecord m : ml) {
				System.out.println("name:" + m.getName() + ", target:" + m.getTarget().toString());
				for(InetAddress ia : instance.findAllByName(m.getTarget().toString())) {
					System.out.println("    ip:" + ia);
				}
			}		
		}
	}
}