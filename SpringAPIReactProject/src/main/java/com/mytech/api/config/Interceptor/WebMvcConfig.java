package com.mytech.api.config.Interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AccessControlInterceptor accessControlInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessControlInterceptor)
                .addPathPatterns("/api/recurrences/userRecurrence/**")
                .addPathPatterns("/api/categories/user/**")
                .addPathPatterns("/api/bills/users/**")
                .addPathPatterns("/api/bills/findBillActive/users/**")
                .addPathPatterns("/api/bills/findBillExpired/users/**")
                .addPathPatterns("/api/wallets/users/**")
                .addPathPatterns("/api/wallets/page/users/**")
                .addPathPatterns("/api/expenses/users/**")
                .addPathPatterns("/api/debts/user/**")
                .addPathPatterns("/api/debts/findDebtPaid/user/**")
                .addPathPatterns("/api/debts/findDebtActive/user/**")
                .addPathPatterns("/api/savinggoals/user/**")
                .addPathPatterns("/api/savinggoals/page/users/**")
                .addPathPatterns("/api/savinggoals/findWorkingByUserId/user/**")
                .addPathPatterns("/api/savinggoals/findFinishedByUserId/user/**")
                .addPathPatterns("/api/incomes/users/**")
                .addPathPatterns("/api/budgets/users/**")
                .addPathPatterns("/api/notifications/user/**")
                .addPathPatterns("/api/transactions/allWallets/users/**")
                .addPathPatterns("/api/transactions/allIncome/users/**")
                .addPathPatterns("/api/transactions/allExpense/users/**")
                .addPathPatterns("/api/transactions/users/**")
                .addPathPatterns("/api/auth/users/**")
                .addPathPatterns("/api/transactionsRecurring/users/**")
                .addPathPatterns("/api/transactions/getTop5NewTransaction/users/**")
                .addPathPatterns("/api/transactions/GetTransactionWithTime/users/**")
                .addPathPatterns("/api/budgets/valid/users/**")
                .addPathPatterns("/api/budgets/not_valid/users/**")
                .addPathPatterns("/api/transactions/users/**");

    }
}
