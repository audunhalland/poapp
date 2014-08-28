//
//  ProductTableViewController.m
//  poapp
//
//  Created by Audun Halland on 28/08/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import "ProductTableViewController.h"
#import "AppDelegate.h"

@interface ProductTableViewController ()
@property (strong, nonatomic) NSFetchedResultsController *fetchController;
@property (strong, nonatomic) UISearchDisplayController *searchController;
@property (strong, nonatomic) NSFetchedResultsController *searchFetchController;
@end

@implementation ProductTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    [NSFetchedResultsController deleteCacheWithName:@"ProductCache"];
    [self setupFetch];

    UISearchBar *bar = [[UISearchBar alloc] init];
    _searchController = [[UISearchDisplayController alloc] initWithSearchBar:bar contentsController:self];
    _searchController.displaysSearchBarInNavigationBar = YES;
    _searchController.searchResultsDataSource = self;
    _searchController.delegate = self;

    NSError *err = nil;

    if (![_fetchController performFetch:&err]) {
        NSLog(@"ProductTableViewController: unable to fetch: %@", [err localizedDescription]);
    }
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (NSManagedObjectContext *)getMOC
{
    AppDelegate *ad = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    return ad.managedObjectContext;
}

- (NSFetchRequest *)makeFetchRequest
{
    NSFetchRequest *fr = [[NSFetchRequest alloc] init];
    [fr setEntity:[NSEntityDescription entityForName:@"Product" inManagedObjectContext:[self getMOC]]];
    [fr setSortDescriptors:
     [NSArray arrayWithObject:[[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES]]];
    return fr;
}

- (void)setupFetch
{


    _fetchController = [[NSFetchedResultsController alloc]
                        initWithFetchRequest:[self makeFetchRequest]
                        managedObjectContext:[self getMOC]
                        sectionNameKeyPath:nil
                        cacheName:@"ProductCache"];
    _searchFetchController = [[NSFetchedResultsController alloc]
                              initWithFetchRequest:[self makeFetchRequest]
                              managedObjectContext:[self getMOC]
                              sectionNameKeyPath:nil
                              cacheName:nil];

    _fetchController.delegate = self;
    _searchFetchController.delegate = self;
}

- (BOOL)searchDisplayController:(UISearchDisplayController *)controller shouldReloadTableForSearchString:(NSString *)searchString
{
    NSPredicate *p = [NSPredicate predicateWithFormat:@"name contains[cd] %@", searchString];

    NSError *err = nil;

    [_searchFetchController.fetchRequest setPredicate:p];
    [_searchFetchController performFetch:&err];

    return YES;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    NSFetchedResultsController * c = (tableView == self.tableView ? _fetchController : _searchFetchController);
    return [[c sections] count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSFetchedResultsController * c = (tableView == self.tableView ? _fetchController : _searchFetchController);
    // Return the number of rows in the section.
    id sectionInfo = [[c sections] objectAtIndex:section];
    return [sectionInfo numberOfObjects];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSFetchedResultsController * c = (tableView == self.tableView ? _fetchController : _searchFetchController);
    UITableViewCell *cell = [self.tableView dequeueReusableCellWithIdentifier:@"default" forIndexPath:indexPath];
    
    // Configure the cell...
    NSManagedObject *product = [c objectAtIndexPath:indexPath];
    cell.textLabel.text = [product valueForKey:@"name"];

    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

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
