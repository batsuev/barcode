#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "barcode.h"
#include "string.h"

#ifdef STRICMP
#define strcasecmp stricmp
#endif

JNIEXPORT jobject JNICALL Java_barcode_BarCode_Create
(JNIEnv *env, jobject jobj, jstring code, jstring encoding)
{
  const char *codeStr = (*env)->GetStringUTFChars(env, code, NULL);
  const char *encodingStr = (*env)->GetStringUTFChars(env, encoding, NULL);

  struct Barcode_Item * bc;
  int bar;

  if (!strcasecmp(encodingStr, "ANY")) bar=BARCODE_ANY;
	else if (!strcasecmp(encodingStr, "EAN")) bar=BARCODE_EAN;
	else if (!strcasecmp(encodingStr, "UPC")) bar=BARCODE_UPC;
	else if (!strcasecmp(encodingStr, "ISBN")) bar=BARCODE_ISBN;
	else if (!strcasecmp(encodingStr, "39")) bar=BARCODE_39;
	else if (!strcasecmp(encodingStr, "128")) bar=BARCODE_128;
	else if (!strcasecmp(encodingStr, "128C")) bar=BARCODE_128C;
	else if (!strcasecmp(encodingStr, "128B")) bar=BARCODE_128B;
	else if (!strcasecmp(encodingStr, "I25")) bar=BARCODE_I25;
	else if (!strcasecmp(encodingStr, "128RAW")) bar=BARCODE_128RAW;
	else if (!strcasecmp(encodingStr, "CBR")) bar=BARCODE_CBR;
	else if (!strcasecmp(encodingStr, "MSI")) bar=BARCODE_MSI;
	else if (!strcasecmp(encodingStr, "PLS")) bar=BARCODE_PLS;
#if BARCODE_VERSION_INT >= 9700
	else if (!strcasecmp(encodingStr, "93")) bar=BARCODE_93;
  else bar=BARCODE_ANY;
#endif

  bc=Barcode_Create((char*) codeStr);
  Barcode_Encode(bc, bar);

  jstring encodingE = (*env)->NewStringUTF(env, bc->encoding);
  jstring textInfoE = (*env)->NewStringUTF(env, bc->textinfo);
  jstring partialF = (*env)->NewStringUTF(env, bc->partial);

  jclass respClass = (*env)->FindClass(env, "barcode/BarCodeResponse");
  jmethodID ctor = (*env)->GetMethodID(env, respClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
  jobject instance = (*env)->NewObject(env, respClass, ctor, encodingE, textInfoE, partialF);

  (*env)->ReleaseStringUTFChars(env, code, codeStr);
  (*env)->ReleaseStringUTFChars(env, encoding, encodingStr);

  return instance;
}
