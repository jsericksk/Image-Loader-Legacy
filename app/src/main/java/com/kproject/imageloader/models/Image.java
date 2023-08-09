package com.kproject.imageloader.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.kproject.imageloader.models.Image;

public class Image implements Parcelable {
	private int imageId;
	private String imageUrl;
	private String imageName;
	private String imagePage;

	public Image(int imageId, String imageUrl, String imageName, String imagePage) {
		this.imageId = imageId;
		this.imageUrl = imageUrl;
		this.imageName = imageName;
		this.imagePage = imagePage;
	}

	public Image(Parcel parcel) {
		imageId = parcel.readInt();
		imageUrl = parcel.readString();
		imageName = parcel.readString();
		imagePage = parcel.readString();
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImagePage(String imagePage) {
		this.imagePage = imagePage;
	}

	public String getImagePage() {
		return imagePage;
	}

	
	@Override
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(imageId);
		dest.writeString(imageUrl);
		dest.writeString(imageName);
		dest.writeString(imagePage);
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj instanceof Image) && ((Image)obj).getImageUrl().equals(this.getImageUrl())) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + imageUrl.hashCode();
		return result;
	}

	public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {

		@Override
		public Image createFromParcel(Parcel parcel) {
			return new Image(parcel);
		}

		@Override
		public Image[] newArray(int size) {
			return new Image[0];
		}

	};

}
