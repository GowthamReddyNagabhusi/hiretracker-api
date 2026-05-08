package com.hiretrack.hiretrack_api.dto;

public class StatsResponse {
    private long totalApplied;
    private long totalInterviews;
    private long totalOffers;
    private long totalRejections;
    private double offerRate;

    public StatsResponse(long totalApplied, long totalInterviews,
                         long totalOffers, long totalRejections, double offerRate) {
        this.totalApplied = totalApplied;
        this.totalInterviews = totalInterviews;
        this.totalOffers = totalOffers;
        this.totalRejections = totalRejections;
        this.offerRate = offerRate;
    }

    public long getTotalApplied() { return totalApplied; }
    public long getTotalInterviews() { return totalInterviews; }
    public long getTotalOffers() { return totalOffers; }
    public long getTotalRejections() { return totalRejections; }
    public double getOfferRate() { return offerRate; }
}