package org.allatra.radio;

import android.os.Build;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class Audio implements Serializable, Cloneable {

    private String data;
    private String title;
    private String album;
    private String artist;

    public Audio(String data, String title, String album, String artist) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @NonNull
    @Override
    public String toString() {
        return "Audio{" +
                "data='" + data + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    @Override
    protected Audio clone(){
        try {
            return (Audio) super.clone();
        } catch (CloneNotSupportedException ex){
            throw new InternalError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Audio audio = (Audio) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(data, audio.data) &&
                    Objects.equals(title, audio.title) &&
                    Objects.equals(album, audio.album) &&
                    Objects.equals(artist, audio.artist);
        } else {
            Audio other = (Audio) o;
            if (!data.equals(other.data))
                return false;
            if (!title.equals(other.title))
                return false;
            if (!album.equals(other.album))
                return false;
            return artist.equals(other.artist);
        }
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(data, title, album, artist);
        } else {
            final int prime = 31;
            int result = 11;
            result = prime * result + data.hashCode();
            result = prime * result + title.hashCode();
            result = prime * result + album.hashCode();
            result = prime * result + artist.hashCode();
            return result;
        }
    }
}
