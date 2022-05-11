package fm.douban.model;

import java.io.Serializable;
import java.util.List;

public class CollectionViewModel implements Serializable {
    private Subject subject;

    private Singer singer;

    private List<Song> songs;

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Singer getSinger() {
        return singer;
    }

    public void setSinger(Singer singer) {
        this.singer = singer;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
