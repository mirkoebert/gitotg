package com.mirkoebert.sgi.aggregator;


import java.util.List;


public record HcpData(List<String> labels, List<Double> hcp) {
}
