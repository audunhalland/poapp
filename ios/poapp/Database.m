//
//  Database.m
//  poapp
//
//  Created by Audun Halland on 07/09/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import "Database.h"

@implementation Database

+ (NSManagedObject *)productForEan:(NSString *)ean
{
    AppDelegate *ad = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    NSManagedObjectContext *moc = ad.managedObjectContext;
    NSFetchRequest *fr = [[NSFetchRequest alloc] init];
    NSError *err = nil;

    [fr setEntity:[NSEntityDescription entityForName:@"Product" inManagedObjectContext:moc]];
    [fr setPredicate:[NSPredicate predicateWithFormat:@"ean == %@", ean]];

    NSArray *a = [moc executeFetchRequest:fr error:&err];

    if (err) {
        NSLog(@"Error: %@", [err localizedDescription]);
        return nil;
    }

    if ([a count] == 1) {
        return [a objectAtIndex:0];
    } else {
        return nil;
    }
}

+ (void)syncWithData:(NSData *)data error:(NSError *__autoreleasing *)errorPtr
{
    NSArray *array = [NSJSONSerialization JSONObjectWithData:data options:0 error:errorPtr];
    if (*errorPtr) return;

    AppDelegate *ad = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    *errorPtr = [ad deleteDatabase];
    if (*errorPtr) return;

    NSManagedObjectContext *moc = ad.managedObjectContext;
    NSMutableDictionary *substances = [[NSMutableDictionary alloc] init];

    {
        NSManagedObject *po = [NSEntityDescription
                               insertNewObjectForEntityForName:@"Substance"
                               inManagedObjectContext:moc];
        [po setValue:@"po" forKey:@"name"];
        [substances setObject:po forKey:@"po"];
    }

    NSLog(@"GOT DATA OF LENGTH %u", [array count]);

    for (id obj in array) {
        NSDictionary *dict = (NSDictionary*)obj;
        NSManagedObject *p = [NSEntityDescription
                              insertNewObjectForEntityForName:@"Product"
                              inManagedObjectContext:moc];

        // BUG: can there be more than one ean for a product?
        [p setValue:[dict objectForKey:@"ean"] forKey:@"ean"];
        [p setValue:[dict objectForKey:@"name"] forKey:@"name"];

        NSMutableSet *badIngredients = [[NSMutableSet alloc] init];

        for (id idict in [dict objectForKey:@"bi"]) {
            NSManagedObject *ingr = [NSEntityDescription
                                     insertNewObjectForEntityForName:@"Ingredient"
                                     inManagedObjectContext:moc];
            [ingr setValue:[idict objectForKey:@"min"] forKey:@"percentageLowerBound"];
            [ingr setValue:[idict objectForKey:@"max"] forKey:@"percentageHigherBound"];
            //since there is only one supported substance (for now):
            [ingr setValue:[substances objectForKey:[idict objectForKey:@"subst"]] forKey:@"substance"];
            [badIngredients addObject:ingr];
        }

        [p setValue:badIngredients forKey:@"badIngredients"];
    }

    if (![moc save:errorPtr]) {
        NSLog(@"Could not save products: %@", [*errorPtr localizedDescription]);
        return;
    }
}

+ (void)syncOverHttpUsingBlock:(void(^)(NSError*))completionBlock
{
    NSString *path = @"http://audunhalland.com/podb/po.php";
    NSURL *url = [[NSURL alloc] initWithString:path];

    [NSURLConnection sendAsynchronousRequest:[[NSURLRequest alloc] initWithURL:url]
                                       queue:[[NSOperationQueue alloc] init]
                           completionHandler:^(NSURLResponse *response, NSData *data, NSError *err) {
                                if (!err) {
                                    [Database syncWithData:data error:&err];
                                }

                                dispatch_async(dispatch_get_main_queue(), ^{
                                       completionBlock(err);
                                });
                           }];
}


@end
