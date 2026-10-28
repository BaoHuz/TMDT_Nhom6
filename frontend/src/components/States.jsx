import React from 'react';
import { Loader2, AlertCircle, Inbox, Sparkles } from 'lucide-react';

export const Loading = ({ message = 'Đang tải dữ liệu hệ thống...' }) => (
  <div className="state-box state-loading">
    <Loader2 size={36} className="state-spinner" />
    <p>{message}</p>
  </div>
);

export const ErrorState = ({ message = 'Đã có lỗi xảy ra khi tải dữ liệu.' }) => (
  <div className="state-box state-error">
    <AlertCircle size={36} className="state-icon-error" />
    <h4>Không thể tải dữ liệu</h4>
    <p>{message}</p>
  </div>
);

export const EmptyState = ({ message = 'Chưa có dữ liệu phù hợp trong mục này.' }) => (
  <div className="state-box state-empty">
    <Inbox size={42} className="state-icon-empty" />
    <h4>Không tìm thấy dữ liệu</h4>
    <p>{message}</p>
  </div>
);

export const PageTitle = ({ eyebrow, title, children }) => (
  <div className="page-title-banner">
    {eyebrow && <span className="eyebrow-chip"><Sparkles size={13} /> {eyebrow}</span>}
    <h1 className="main-heading">{title}</h1>
    {children && <p className="sub-text">{children}</p>}
  </div>
);
