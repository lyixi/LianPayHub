package com.lianpayhub.web.api;

import com.lianpayhub.domain.app.AppInfo;
import com.lianpayhub.domain.payment.PayChannel;
import com.lianpayhub.domain.product.ProductInfo;
import com.lianpayhub.domain.product.ProductPlan;
import com.lianpayhub.domain.purchase.PurchasePageConfig;
import java.util.List;

public class PublicPurchasePageResult {
    private final PurchasePageConfig page;
    private final AppInfo app;
    private final List<ProductInfo> products;
    private final List<ProductPlan> plans;
    private final List<PayChannel> payChannels;

    public PublicPurchasePageResult(PurchasePageConfig page, AppInfo app, List<ProductInfo> products, List<ProductPlan> plans,
                                    List<PayChannel> payChannels) {
        this.page = page;
        this.app = app;
        this.products = products;
        this.plans = plans;
        this.payChannels = payChannels;
    }

    public PurchasePageConfig getPage() { return page; }
    public AppInfo getApp() { return app; }
    public List<ProductInfo> getProducts() { return products; }
    public List<ProductPlan> getPlans() { return plans; }
    public List<PayChannel> getPayChannels() { return payChannels; }
}
