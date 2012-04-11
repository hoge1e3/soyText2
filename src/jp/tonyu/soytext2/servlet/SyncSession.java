package jp.tonyu.soytext2.servlet;

import java.util.HashSet;
import java.util.Set;

import jp.tonyu.soytext2.js.DocumentScriptable;

public class SyncSession {
	Set<String> uploadIds=new HashSet<String>();
	Set<String> downloadIds=new HashSet<String>();
	DocumentScriptable profile;
	HttpContext ctx;
	long newRemoteLastSynced;

	public SyncSession(HttpContext ctx, DocumentScriptable profile) {
		super();
		this.ctx=ctx;
		this.profile = profile;
	}
	public void confirm() {
		requestUpdatedIds();
		calcUpdatedLocalIds();
	}
	private void calcUpdatedLocalIds() {
		// TODO 自動生成されたメソッド・スタブ

	}
	public void exec() {

	}

	public void requestUpdatedIds() {
		// updatedIds?since=remoteLastSynced
	}
	public void responseUpdatedIds() {

	}

	public void requestUploadDocuments() {
		// get from session

	}
	public void responseUploadDocuments() {

	}
	public void requestDownloadDocuments() {

	}
	public void responsetDownloadDocuments() {

	}
}
