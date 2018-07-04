LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := IRDecoder
LOCAL_SRC_FILES := DecodeIR.cpp
LOCAL_C_INCLUDES += /home/ocean/workspace/android/android-ndk-r5b/sources/cxx-stl/stlport/stlport
LOCAL_LDLIBS := /home/ocean/workspace/android/android-ndk-r5b/sources/cxx-stl/stlport/libs/armeabi/libstlport_static.a

include $(BUILD_SHARED_LIBRARY)