package com.mirkoebert.sgi.chart;


import java.util.List;


public record HcpData(List<String> labels, List<Double> hcp) {
}
