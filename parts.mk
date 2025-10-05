# Sepolicy
BOARD_SEPOLICY_DIRS += packages/apps/XiaomiParts/sepolicy

ifeq ($(TARGET_USE_CLEARSPEAKER),true)
PRODUCT_PACKAGES += \
    ClearSpeaker
endif
