//
//  ViewController.m
//  testAlbum
//
//  Created by miaojun on 15/9/20.
//  Copyright © 2015年 miaojun. All rights reserved.
//

#import "ViewController.h"
#import <AssetsLibrary/AssetsLibrary.h>

@interface ViewController ()

@property (nonatomic,strong) UIImageView * imageView;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    self.imageView = [[UIImageView alloc] initWithFrame:self.view.bounds];
    [self.view addSubview:self.imageView];
    self.imageView.image = [UIImage imageNamed:@"huba.jpeg"];
    
    UIButton * button = [[UIButton alloc] init];
    [button setTitle:@"save to album" forState:UIControlStateNormal];
    [button sizeToFit];
    //button.backgroundColor = [UIColor blueColor];
    [button addTarget:self action:@selector(onPress) forControlEvents:UIControlEventTouchUpInside];
    button.center = CGPointMake(self.view.bounds.size.width/2, 300);
    [self.view addSubview:button];
}

- (void)onPress
{
    [self createAlbumInPhoneAlbum];

    //UIImageWriteToSavedPhotosAlbum(self.imageView.image, nil, nil, nil);
}



#pragma mark - 在手机相册中创建相册
- (void)createAlbumInPhoneAlbum
{
    ALAssetsLibrary *assetsLibrary;
    NSMutableArray *groupArray;
    assetsLibrary = [[ALAssetsLibrary alloc] init];
    groupArray=[[NSMutableArray alloc] initWithCapacity:1];
    [assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupAll usingBlock:^(ALAssetsGroup *group, BOOL *stop) {
        if (group) {
            [groupArray addObject:group];
            
            //            通过这个可以知道相册的名字，从而也可以知道安装的部分应用
            //例如 Name:柚子相机, Type:Album, Assets count:1
            NSLog(@"%@",group);
            
            [group enumerateAssetsUsingBlock:^(ALAsset *result, NSUInteger index, BOOL *stop) {
                if (result) {
                    //[imageArray addObject:result];
                    NSLog(@"%@",result);
                    //iv.image=[UIImage imageWithCGImage: result.thumbnail];
                    NSString *type=[result valueForProperty:ALAssetPropertyType];
                }
            }];
            
        }
    } failureBlock:^(NSError *error) {
        NSLog(@"Group not found!\n");
    }];
    
    /*
    [assetsLibrary addAssetsGroupAlbumWithName:@"匪兵1" resultBlock:^(ALAssetsGroup *group) {
        
        
    } failureBlock:^(NSError *error) {
        
    }];
     */
    
    
    void (^AddSetBlocK)(ALAssetsLibrary *, NSURL *) = ^(ALAssetsLibrary *assetsLibrary1, NSURL *assetURL){
        [assetsLibrary1 assetForURL:assetURL resultBlock:^(ALAsset *asset) {
            [assetsLibrary enumerateGroupsWithTypes:ALAssetsGroupAll usingBlock:^(ALAssetsGroup *group, BOOL *stop) {
                if (group) {
                    NSLog(@"%@",[group valueForProperty:ALAssetsGroupPropertyName]);
                    if ([[group valueForProperty:ALAssetsGroupPropertyName] isEqualToString:@"b"])
                    {
                        [group addAsset:asset];
                        
                    }
                }
            } failureBlock:^(NSError *error) {
                NSLog(@"Group not found!\n");
            }];

        } failureBlock:^(NSError *error) {
            
        }];
            
       
    };
    
    
    
    __weak ALAssetsLibrary *weakSelf = assetsLibrary;
    [assetsLibrary writeImageDataToSavedPhotosAlbum:UIImagePNGRepresentation(self.imageView.image) metadata:nil completionBlock:^(NSURL *assetURL, NSError *error) {
        AddSetBlocK(weakSelf,assetURL);
        
    }];
}



@end
