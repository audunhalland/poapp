//
//  ScanViewController.m
//  poapp
//
//  Created by Audun Halland on 26/08/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import "ScanViewController.h"

@interface ScanViewController ()
@property (weak, nonatomic) IBOutlet UIView *previewView;

@end

@implementation ScanViewController {
    AVCaptureSession *_session;
    AVCaptureVideoPreviewLayer *_previewLayer;
    BOOL _capturing;
    AVCaptureMetadataOutput *_metadataOutput;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if ([self setupCaptureSession]) {
        _previewLayer.frame = _previewView.bounds;
        [_previewView.layer addSublayer:_previewLayer];
        
        _metadataOutput.metadataObjectTypes = @[@"org.gs1.EAN-13"];
    } else {
        NSLog(@"Error setting up capture session");
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self startCapture];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self stopCapture];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)setupCaptureSession
{
    if (_session) return YES;
    
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    
    if (!device) {
        return NO;
    }
    
    _session = [[AVCaptureSession alloc] init];
    
    [_session addInput:[[AVCaptureDeviceInput alloc] initWithDevice:device error:nil]];
    
    _previewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:_session];
    _previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    
    _metadataOutput = [[AVCaptureMetadataOutput alloc] init];
    [_metadataOutput setMetadataObjectsDelegate:self
                                          queue:dispatch_queue_create("com.audunhalland.poapp.metadata", 0)];
    
    [_session addOutput:_metadataOutput];
    
    return YES;
}

- (void)startCapture
{
    if (_capturing) return;
    [_session startRunning];
    _capturing = YES;
}

- (void)stopCapture
{
    if (!_capturing) return;
    [_session stopRunning];
    _capturing = NO;
}

- (void)foundCode:(NSString *)code
{
    // This runs on the custom dispatch queue
    [self stopCapture];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertView *av = [[UIAlertView alloc] initWithTitle:code
                                                     message:@""
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles:@"Stuff", nil];
        [av show];
    });
    
}

- (void)captureOutput:(AVCaptureOutput *)captureOutput didOutputMetadataObjects:(NSArray *)metadataObjects fromConnection:(AVCaptureConnection *)connection
{
    [metadataObjects enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        AVMetadataMachineReadableCodeObject *code = (AVMetadataMachineReadableCodeObject*)
            [_previewLayer transformedMetadataObjectForMetadataObject:obj];
        
        [self foundCode:[NSString stringWithString:code.stringValue]];
        return;
    }];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
