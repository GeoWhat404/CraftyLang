package me.geowhat.craftylang.crs;

public class CRSMath {
    public static final String code =
            """
                    let PI = 3.14159265358979;

                    fn min(a, b) {
                      if (a > b) {
                        ret b;
                      }
                      ret a;
                    }

                    fn max(a, b) {
                      if (a > b) {
                        ret a;
                      }
                      ret b;
                    }

                    fn abs(a) {
                      if (a > 0 | a == 0) {
                        ret a;
                      }
                      ret -a;
                    }

                    fn avg(a, b) {
                      ret (a + b) / 2;
                    }

                    fn sqrt(n) {
                      let l = min(1, n);
                      let h = max(1, n);
                      let m = 0;

                      while(100*l*l<n) {
                        l = l * 10;
                      }
                      while (0.01*h*h>n) {
                        h = h * 0.1;
                      } \s
                      for(let i=0;i<100;i=i+1) {
                        m = (l+h)/2;
                        if (m*m==n) {
                          ret m;
                        } if (m*m > n) {
                          h = m;
                        } else {
                          l = m;
                        }
                      }\s
                      ret m;   \s
                    }

                    fn pow(a, e) {
                      if (e == 0 & a == 0) {
                        ret 1;
                      } if (e == 1) {
                        ret a;
                      } if (e < 0) {
                        ret pow(a, -e);
                      } else {
                        let r = 1;
                        while (e > 0) {
                          r = r * a;
                          e = e - 1;
                        }
                        ret r;
                      }
                    }

                    fn hypot(a, b) {
                      ret sqrt(pow(a, 2) + pow(b, 2));
                    }""";
}
