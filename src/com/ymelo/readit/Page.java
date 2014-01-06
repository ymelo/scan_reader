package com.ymelo.readit;

import android.os.Parcel;
import android.os.Parcelable;

public class Page implements Parcelable{
	public String bookId;
	public String chapterId;
	public String pageId;
	public String localResourceUri;
	public String remoteResourceUrl;
	
	public Page(String bookId, String chapterId, String pageId,
			String localResourceUri, String onlineResourceUrl) {
		super();
		this.bookId = bookId;
		this.chapterId = chapterId;
		this.pageId = pageId;
		this.localResourceUri = localResourceUri;
		this.remoteResourceUrl = onlineResourceUrl;
	}
	
	public String getLocalDirectory() {
		int index = localResourceUri.lastIndexOf("/");
		return localResourceUri.substring(index);
	}


	private Page(Parcel in) {
		this.bookId = in.readString();
		this.chapterId = in.readString();
		this.pageId = in.readString();
		this.localResourceUri = in.readString();
		this.remoteResourceUrl = in.readString();
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(bookId);
		dest.writeString(chapterId);
		dest.writeString(pageId);
		dest.writeString(localResourceUri);
		dest.writeString(remoteResourceUrl);
	}
	public static final Parcelable.Creator<Page> CREATOR = new Parcelable.Creator<Page>() {
		public Page createFromParcel(Parcel in) {
			return new Page(in);
		}

		public Page[] newArray(int size) {
			return new Page[size];
		}
	};



}
