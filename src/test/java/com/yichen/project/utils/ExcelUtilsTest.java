package com.yichen.project.utils;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilsTest {

    @Test
    void excelToCsv() {
        ExcelUtils.excelToCsv(null);
    }
}