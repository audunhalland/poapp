//
//  MainViewController.m
//  poapp
//
//  Created by Audun Halland on 26/08/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import "MainViewController.h"
#import "ScanViewController.h"
#import "AppDelegate.h"
#import "Database.h"

@interface MainViewController ()
@property (weak, nonatomic) IBOutlet UIBarButtonItem *testRefreshButton;
@property (weak, nonatomic) IBOutlet UIButton *scanButton;

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

- (IBAction)refresh:(id)sender
{
    [self testSync];
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
    NSManagedObject *product = [Database productForEan:code];

    if (product) {
        NSSet *badIngredients = [product valueForKey:@"badIngredients"];
        NSString *desc = nil;

        if ([badIngredients count] == 0) {
            desc = @"no bad ingredients";
        } else {
            NSMutableString *s = [[NSMutableString alloc] init];
            for (NSManagedObject *bi in badIngredients) {
                NSString *bis = [NSString stringWithFormat:@"contains %@-%@%% %@\n",
                                  [bi valueForKey:@"percentageLowerBound"],
                                  [bi valueForKey:@"percentageHigherBound"],
                                 [[bi valueForKey:@"substance"] valueForKey:@"name"]];

                [s appendString: bis];
            }
            desc = s;
        }

        [[[UIAlertView alloc] initWithTitle:[product valueForKey:@"name"]
                                    message:desc
                                   delegate:self
                          cancelButtonTitle:@"Cancel"
                          otherButtonTitles:@"Stuff", nil] show];
    } else {
        [[[UIAlertView alloc] initWithTitle:@"Not found"
                                    message:code
                                   delegate:self
                          cancelButtonTitle:@"Cancel"
                          otherButtonTitles:@"Stuff", nil] show];
    }
}

- (void)testSync
{
    [_testRefreshButton setEnabled:NO];
    [_scanButton setEnabled:NO];

    [Database syncOverHttpUsingBlock:^(NSError *err) {
        if (err) {
            [self syncError:err];
        }
        [_testRefreshButton setEnabled:YES];
        [_scanButton setEnabled:YES];
    }];
}

- (void)syncError:(NSError *)error
{
    NSLog(@"SYNC ERROR: %@", [error localizedDescription]);
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
