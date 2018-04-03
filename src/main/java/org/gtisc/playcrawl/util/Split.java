/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.util;

import java.util.List;

/**
 *
 * @author meng
 */
public class Split {

    public static <T> List<T>[] splitByBinsize(List<T> elements, int binsize) {
        int total = elements.size();
        int num = total / binsize;
        if (total % binsize != 0) {
            num++;
        }

        List<T>[] result = new List[num];

        for (int i = 0; i < num - 1; i++) {
            result[i] = elements.subList(binsize * i, binsize * (i + 1));
        }
        result[num - 1] = elements.subList(binsize * (num - 1), total);

        return result;
    }
}
