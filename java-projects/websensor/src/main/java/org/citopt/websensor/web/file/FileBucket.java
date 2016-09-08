package org.citopt.websensor.web.file;

import org.springframework.web.multipart.MultipartFile;

public class FileBucket {

    MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "FileBucket{" + "file=" + file + '}';
    }

}
