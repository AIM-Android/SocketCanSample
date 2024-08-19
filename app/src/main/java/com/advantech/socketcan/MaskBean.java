package com.advantech.socketcan;

public class MaskBean {
    private int filterId;
    private int mask;

    public MaskBean(int filterId, int mask) {
        this.filterId = filterId;
        this.mask = mask;
    }

    public int getFilterId() {
        return filterId;
    }

    public void setFilterId(int filterId) {
        this.filterId = filterId;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    @Override
    public String toString() {
        return "MaskBean{" +
                "filterId=" + filterId +
                ", mask=" + mask +
                '}';
    }
}
