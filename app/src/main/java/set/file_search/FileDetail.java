package set.file_search;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android on 11/20/17.
 */

public class FileDetail {

    private String file;
    @SerializedName("relevance_score")
    private double relevanceScore;

    public FileDetail(String file, double relevanceScore) {
        this.file = file;
        this.relevanceScore = relevanceScore;
    }

    public String getFile() {
        return file;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }
}
