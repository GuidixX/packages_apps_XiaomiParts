# Sepolicy
BOARD_SEPOLICY_DIRS += packages/apps/XiaomiParts/sepolicy

# Soong
PRODUCT_SOONG_NAMESPACES += \
    packages/apps/XiaomiParts

ifeq ($(TARGET_USE_CLEARSPEAKER),true)
PRODUCT_PACKAGES += \
    ClearSpeaker
endif
ifeq ($(TARGET_USE_SATURATIONSLIDER),true)
PRODUCT_PACKAGES += \
    Saturation
endif
