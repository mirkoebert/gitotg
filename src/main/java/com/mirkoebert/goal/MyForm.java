package com.mirkoebert.goal;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MyForm {
        private List<Long> selectedOptions = new ArrayList<>();
}
