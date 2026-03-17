package com.vaultpool.customer.data.remote.dto.staff;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ImageStaffDto implements Serializable {
    @SerializedName("id")
    private Long id;
    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("sortOrder")
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
