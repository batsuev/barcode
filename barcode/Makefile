CC=gcc
jhome=$(shell jrunscript -e 'java.lang.System.out.println(java.lang.System.getProperty("java.home"));')/..

CFLAGS=-DIS_LIBRARY -DHAVE_CONFIG_H -g -O2 -I/usr/local/opt/gettext/include -Ilib -fPIC
INCLUDES=-Ibarcode-0.99 -Ibarcode-0.99/lib -I$(jhome)/include -I$(jhome)/include/darwin -I$(jhome)/include/linux
LDFLAGS=-Lbarcode-0.99/lib -L/usr/local/opt/gettext/lib -lm -fPIC

OBJS= \
  barcode_BarCode.o \
  barcode-0.99/library.o \
  barcode-0.99/ean.o \
  barcode-0.99/code128.o \
  barcode-0.99/code39.o \
  barcode-0.99/code93.o \
  barcode-0.99/i25.o \
  barcode-0.99/msi.o \
  barcode-0.99/plessey.o \
  barcode-0.99/codabar.o \
  barcode-0.99/ps.o \
  barcode-0.99/svg.o \
  barcode-0.99/pcl.o \
  barcode-0.99/code11.o

all: src
	$(CC) -shared -o libbarcode.so $(OBJS) $(LDFLAGS)

%.o : %.c
	$(CC) $(INCLUDES) $(CFLAGS) -o $@ -c $<

src: $(OBJS)

clean:
	rm -rf $(OBJS) libgnubg.so
