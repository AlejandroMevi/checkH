package com.venturessoft.human;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void sumar4() throws Exception{
        assertEquals(48, numeroa() + numerob());
    }
    public int numeroa(){
        return 5;
    }
    public int numerob(){
        return 6;
    }
}
