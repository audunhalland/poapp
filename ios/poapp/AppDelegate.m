//
//  AppDelegate.m
//  poapp
//
//  Created by Audun Halland on 26/08/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import "AppDelegate.h"

@implementation AppDelegate {
    NSPersistentStore* _store;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    return [self initCoreData];
}

- (NSURL *)storeURL
{
    NSString *path = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask, YES) lastObject];
    return [NSURL fileURLWithPath:[path stringByAppendingPathComponent: @"Products.sqlite"]];
}

- (BOOL)initCoreData
{
    NSError *error = nil;
    
    _managedObjectModel = [NSManagedObjectModel mergedModelFromBundles:nil];
    _persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:_managedObjectModel];

    _store = [_persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType
                                                       configuration:nil
                                                                 URL:[self storeURL]
                                                             options:nil
                                                               error:&error];
    
    if (!_store) {
        NSLog(@"problem initializing store coordinator: %@", error);
        return NO;
    }
    
    _managedObjectContext = [[NSManagedObjectContext alloc] init];
    [_managedObjectContext setPersistentStoreCoordinator:_persistentStoreCoordinator];

    return YES;
}

- (NSError*)deleteDatabase
{
    NSError *err = nil;
    [_persistentStoreCoordinator removePersistentStore:_store error:&err];
    if (err) return err;
    [[NSFileManager defaultManager] removeItemAtPath:[self storeURL].path error:&err];
    if (err) return err;
    _store = [_persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType
                                                       configuration:nil
                                                                 URL:[self storeURL]
                                                             options:nil
                                                               error:&err];
    return err;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
