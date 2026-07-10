package com.FinalProject.group3.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Địa chỉ nhận hàng (LA.Address / DL_Add New Address).
 * Lưu ở subcollection: customers/{uid}/addresses
 * LƯU Ý: KHÔNG ghi field addressId vào document (@DocumentId tự map).
 */
public class Address {

    @DocumentId
    private String addressId;
    private String name;      // tên người nhận
    private String phone;
    private String province;  // Tỉnh/Thành phố
    private String district;  // Quận/Huyện
    private String ward;      // Phường/Xã
    private String detail;    // địa chỉ chi tiết (số nhà, đường...)
    private boolean isDefault;
    @ServerTimestamp
    private Date createdAt;

    public Address() {}

    public Address(String name, String phone, String province, String district,
                   String ward, String detail, boolean isDefault) {
        this.name = name;
        this.phone = phone;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detail = detail;
        this.isDefault = isDefault;
    }

    /** Địa chỉ đầy đủ 1 dòng: "12 Nguyễn Huệ, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh" */
    public String fullAddress() {
        StringBuilder sb = new StringBuilder();
        if (detail   != null && !detail.isEmpty())   sb.append(detail);
        if (ward     != null && !ward.isEmpty())     sb.append(sb.length() > 0 ? ", " : "").append(ward);
        if (district != null && !district.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(district);
        if (province != null && !province.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(province);
        return sb.toString();
    }

    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
