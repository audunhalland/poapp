//
//  ScanViewController.h
//  poapp
//
//  Created by Audun Halland on 26/08/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import <UIKit/UIKit.h>

@import AVFoundation;

@interface ScanViewController : UIViewController<AVCaptureMetadataOutputObjectsDelegate>
@property NSString *scannedCode;
@end
