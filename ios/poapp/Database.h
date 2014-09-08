//
//  Database.h
//  poapp
//
//  Created by Audun Halland on 07/09/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AppDelegate.h"

@interface Database : NSObject
+ (NSManagedObject*)productForEan:(NSString*)ean;
+ (void)syncWithData:(NSData*)data error:(NSError**)errorPtr;
+ (void)syncOverHttpUsingBlock:(void(^)(NSError*))completionBlock;
@end
