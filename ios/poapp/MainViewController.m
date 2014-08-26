//
//  MainViewController.m
//  poapp
//
//  Created by Audun Halland on 26/08/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import "MainViewController.h"
#import "ScanViewController.h"

@interface MainViewController ()

@end

@implementation MainViewController

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
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)didUnwindTo:(UIStoryboardSegue *)segue
{
    UIViewController *source = segue.sourceViewController;
    
    if ([source isKindOfClass:[ScanViewController class]]) {
        ScanViewController *scv = (ScanViewController *)source;
        
        if (scv.scannedCode) {
            [self handleScannedCode:scv.scannedCode];
        }
    }
}

- (void)handleScannedCode:(NSString*)code
{
    UIAlertView *av = [[UIAlertView alloc] initWithTitle:code
                                                 message:@""
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Stuff", nil];
    [av show];
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
