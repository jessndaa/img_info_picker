#import "ImgInfoPickerPlugin.h"
#if __has_include(<img_info_picker/img_info_picker-Swift.h>)
#import <img_info_picker/img_info_picker-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "img_info_picker-Swift.h"
#endif

@implementation ImgInfoPickerPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftImgInfoPickerPlugin registerWithRegistrar:registrar];
}
@end
