# Sepolicy
BOARD_SEPOLICY_DIRS += packages/apps/XiaomiParts/sepolicy

ifeq ($(TARGET_USE_CLEARSPEAKER),true)
PRODUCT_PACKAGES += \
    ClearSpeaker
endif
ifeq ($(TARGET_USE_SATURATIONSLIDER),true)
PRODUCT_PACKAGES += \
    Saturation
endif
