//
//  syncTests.m
//  poapp
//
//  Created by Audun Halland on 07/09/14.
//  Copyright (c) 2014 Audun Halland. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "Database.h"

@interface syncTests : XCTestCase

@end

@implementation syncTests

- (void)setUp
{
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)syncJSON:(NSString*)data error:(NSError**)errorPtr
{
    [Database syncWithData:[data dataUsingEncoding:NSUTF8StringEncoding] error:errorPtr];
}

- (void)syncJSONError:(NSString*)data
{
    NSError *err = nil;
    [self syncJSON:data error:&err];
    XCTAssertNotNil(err, @"Expected error");
}

- (void)syncOneInvalidProduct:(NSDictionary*)product
{
    NSError *encodeErr = nil;
    NSError *syncErr = nil;
    NSArray *lst = [NSArray arrayWithObject:product];
    [Database syncWithData:
     [NSJSONSerialization dataWithJSONObject:lst options:NSJSONWritingPrettyPrinted error:&encodeErr]
                     error:&syncErr];
    XCTAssertNil(encodeErr, @"error in encode");
    XCTAssertNotNil(syncErr, @"expected sync error");
}

- (void)testIllFormattedJSON
{
    [self syncJSONError:@"hei"];
    [self syncJSONError:@"2"];
    [self syncJSONError:@"[}"];
}

- (void)testSyncNoProducts
{
    NSError *err;
    [self syncJSON:@"[]" error:&err];
    XCTAssertNil(err);
}

- (void)testSyncEmptyProduct
{
    [self syncJSONError:@"[{}]"];
}

- (void)testSyncLimitedProduct1
{
    [self syncOneInvalidProduct:[NSDictionary dictionaryWithObjectsAndKeys:@"ean", @"111", nil]];
}

@end
