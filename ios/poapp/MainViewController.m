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
    UIAlertView *av = [[UIAlertView alloc] initWithTitle:code
                                                 message:@""
                                                delegate:self
                                       cancelButtonTitle:@"Cancel"
                                       otherButtonTitles:@"Stuff", nil];
    [av show];
}

- (void)testSync
{
    NSString *path = @"http://audunhalland.com/podb/po.php";
    NSURL *url = [[NSURL alloc] initWithString:path];
    
    [NSURLConnection sendAsynchronousRequest:[[NSURLRequest alloc] initWithURL:url]
                                       queue:[[NSOperationQueue alloc] init]
                           completionHandler:^(NSURLResponse *response, NSData *data, NSError *err) {
                               if (err) {
                                   [self syncError:err];
                               } else {
                                   [self receivedData:data];
                               }
                           }];
}

- (void)receivedData:(NSData *)data
{
    NSError *err = nil;
    NSArray *array = [NSJSONSerialization JSONObjectWithData:data options:0 error:&err];
    
    if (err) {
        NSLog(@"Error in JSON: %@", [err localizedDescription]);
    }
    
    AppDelegate *ad = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    NSManagedObjectContext *moc = ad.managedObjectContext;
    
    NSLog(@"GOT DATA OF LENGTH %u", [array count]);
    
    for (id obj in array) {
        NSDictionary *dict = (NSDictionary*)obj;
        NSManagedObject *p = [NSEntityDescription
                              insertNewObjectForEntityForName:@"Product" inManagedObjectContext:moc];
        [p setValue:[dict objectForKey:@"ean"] forKey:@"ean"];
        [p setValue:[dict objectForKey:@"name"] forKey:@"name"];
    }
    
    if (![moc save:&err]) {
        NSLog(@"Could not save products: %@", [err localizedDescription]);
    }
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
